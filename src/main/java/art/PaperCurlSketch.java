package art;

import processing.core.*;

public class PaperCurlSketch extends PApplet {

    long seed;

    // Canvas
    int W = 900, H = 900;
    float margin = 55;

    // Density / look
    int ribbons = 1400;
    int maxSteps = 55;
    float stepSize = 9.5f;
    float baseWidth = 12f;

    // Motion field
    float noiseScale = 0.0065f;
    float curliness = 0.85f;

    // Transparency
    float alphaBase = 42f;

    // Palette (paper + colorful accents)
    int[] accents;
    int paperLight, paperMid, paperDark;

    public static void main(String[] args) {
        PApplet.main(PaperCurlSketch.class);
    }

    public void settings() {
        size(W, H);
        pixelDensity(2);
        smooth(8);
    }

    public void setup() {
        initPalette();
        // draw() is called
        noLoop();
    }

    void initPalette() {
        paperLight = color(250, 249, 247);
        paperMid   = color(235, 233, 228);
        paperDark  = color(210, 207, 200);

        accents = new int[] {
                color(236, 80, 80),   // red
                color(246, 160, 60),  // orange
                color(250, 220, 80),  // yellow
                color(70, 190, 120),  // green
                color(70, 150, 235),  // blue
                color(170, 90, 220),  // purple
                color(245, 90, 170),  // pink
                color(30, 180, 190)   // cyan
        };
    }

    public void draw() {
        // Different each run
        seed = System.currentTimeMillis();
        randomSeed(seed);
        noiseSeed(seed);

        // Background (paper with tiny grain)
        background(paperLight);

        // Draw ribbons
        for (int i = 0; i < ribbons; i++) {
            PVector p = randomPointInFrame();
            drawRibbon(p.x, p.y);
        }

        // Frame
        stroke(40, 90);
        strokeWeight(3);
        rect(margin, margin, width - 2 * margin, height - 2 * margin);
    }

    PVector randomPointInFrame() {
        return new PVector(random(margin, width - margin), random(margin, height - margin));
    }

    void drawRibbon(float x0, float y0) {

        // where the ribbon is pointing right now (instant direction)
        float ang = flowAngle(x0, y0);

        // total length ≈ steps × localStep
        int steps = (int) random(maxSteps * 0.55f, maxSteps * 1.15f); // number of dots
        float localStep = stepSize * random(0.75f, 1.25f); // how far dots are
        float w = baseWidth * random(0.55f, 1.35f);

        // Choose color: mostly paper tones, sometimes accent
        int baseCol = choosePaperOrAccent();

        PVector[] pts = new PVector[steps]; // array of points that stores the entire path of one ribbon
        pts[0] = new PVector(x0, y0);

        // how strongly the ribbon is allowed to bend (turning intensity)
        float localCurl = curliness * random(0.6f, 1.4f);

        for (int i = 1; i < steps; i++) { // generating the path
            PVector p = pts[i - 1];

            // How do we follow the flow field without snapping abruptly to it?
            float target = flowAngle(p.x, p.y); // ideal direction at this point in space
            ang = lerpAngle(ang, target, 0.22f); // 78% of the old direction (ang), 22% of the new desired direction (target)

            // encourage curls/loops
            // if i comment, looks straight and boring
            ang += (noise(p.x * 0.012f, p.y * 0.012f, (float)seed * 0.0001f) - 0.5f) * (1.7f * localCurl);
            // if i comment, looks too perfect and digital
            ang += random(-0.2f, 0.2f) * localCurl;

            float nx = p.x + cos(ang) * localStep;
            float ny = p.y + sin(ang) * localStep;

            // bounce inside frame
            if (nx < margin || nx > width - margin) ang = PI - ang;
            if (ny < margin || ny > height - margin) ang = -ang;

            nx = constrain(nx, margin, width - margin);
            ny = constrain(ny, margin, height - margin);

            pts[i] = new PVector(nx, ny);
        }

        // Draw as shaded QUAD_STRIP
        noStroke();
        beginShape(QUAD_STRIP);

        // opacity for the entire ribbon
        float a = alphaBase * random(0.65f, 1.25f);

        for (int i = 0; i < pts.length; i++) { // drawing the ribbon
            PVector p = pts[i]; // Get the current point on the path
            PVector dir = tangent(pts, i); // Compute direction of the path (tangent)
            PVector n = new PVector(-dir.y, dir.x); // Compute the perpendicular direction (normal)
            n.normalize();

            // Compute local ribbon width
            float wf = w * (0.8f + 0.45f * noise(p.x * 0.01f, p.y * 0.01f, (float)seed * 0.0002f));

            // Compute rolling-paper shading, one side bright, one side dark
            float shade = 0.5f + 0.5f * sin(i * 0.35f + noise(p.x * 0.02f, p.y * 0.02f) * TWO_PI);

            int light = lerpColor(baseCol, color(255), 0.30f + 0.40f * shade);
            int dark  = lerpColor(baseCol, color(0),   0.14f + 0.26f * (1f - shade));

            fill(light, a);
            vertex(p.x + n.x * wf, p.y + n.y * wf);

            fill(dark, a);
            vertex(p.x - n.x * wf, p.y - n.y * wf);
        }
        endShape();

        // highlight line occasionally
        if (random(1) < 0.20f) {
            stroke(255, 55);
            strokeWeight(random(0.5f, 1.2f));
            noFill();
            beginShape();
            for (int i = 0; i < pts.length; i += 2) vertex(pts[i].x, pts[i].y);
            endShape();
        }
    }

    int choosePaperOrAccent() {
        float r = random(1);

        if (r < 0.72f) {
            // paper-ish
            int c = lerpColor(paperMid, paperLight, random(0.0f, 0.7f));
            if (random(1) < 0.25f) c = lerpColor(c, paperDark, random(0.05f, 0.3f));
            return c;
        } else {
            // accent, softened toward paper
            int a = accents[(int) random(accents.length)];
            return lerpColor(a, paperMid, random(0.15f, 0.55f));
        }
    }

    float flowAngle(float x, float y) {
        float n = noise(x * noiseScale, y * noiseScale, (float)seed * 0.00015f);
        return n * TWO_PI * 2.1f;
    }

    PVector tangent(PVector[] pts, int i) {
        if (i == 0) return PVector.sub(pts[1], pts[0]).normalize();
        if (i == pts.length - 1) return PVector.sub(pts[i], pts[i - 1]).normalize();
        return PVector.sub(pts[i + 1], pts[i - 1]).normalize();
    }

    float lerpAngle(float a, float b, float t) {
        float d = atan2(sin(b - a), cos(b - a));
        return a + d * t;
    }
}
