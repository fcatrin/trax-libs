#include <stdio.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <libs/nanovg/nanovg.h>
#include <libs/nanovg/nanovg_gl.h>
#include <common.h>

static struct NVGcontext* vg;

void ui_init() {
	vg = nvgCreateGLES2(NVG_ANTIALIAS | NVG_STENCIL_STROKES | NVG_DEBUG);
	if (!vg) {
		log_error("Could not init nanovg ");
	}

	int font = nvgCreateFont(vg, "sans", "/home/fcatrin/git/simusplayer/src/frontend/linux/fonts/Roboto-Regular.ttf");
	if (font < 0) {
		log_error("Could not add font");
	}
}

void ui_render(int width, int height) {
	nvgBeginFrame(vg, width, height, 1.0f);
	nvgBeginPath(vg);
	nvgRect(vg, 100,100, 120,30);
	nvgFillColor(vg, nvgRGBA(255,192,0,255));
	nvgFill(vg);

	nvgSave(vg);

	nvgFontSize(vg, 18.0f);
	nvgFontFace(vg, "sans");
	// nvgTextAlign(vg,NVG_ALIGN_CENTER|NVG_ALIGN_MIDDLE);

	// nvgFontBlur(vg,2);
	nvgFillColor(vg, nvgRGBA(0,0,0,192));
	nvgText(vg, 50, 50, "Text Testing", NULL);

	nvgRestore(vg);

	nvgEndFrame(vg);
}
