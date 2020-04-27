#include <GL/gl.h>
#include <GL/glext.h>
#include <libs/nanovg/nanovg.h>
#include <libs/nanovg/nanovg_gl.h>

static struct NVGcontext* vg;

void ui_init() {
	vg = nvgCreateGL2(NVG_ANTIALIAS | NVG_STENCIL_STROKES | NVG_DEBUG);
	if (!vg) {
		printf("Could not init nanovg.\n");
	}
}

void ui_render(int width, int height) {
	nvgBeginFrame(vg, width, height, 1.0f);
	nvgBeginPath(vg);
	nvgRect(vg, 100,100, 120,30);
	nvgFillColor(vg, nvgRGBA(255,192,0,255));
	nvgFill(vg);
	nvgEndFrame(vg);
}
