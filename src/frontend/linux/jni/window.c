#include <SDL2/SDL.h>
#include <SDL2/SDL_opengl.h>
#include <GL/gl.h>
#include <formats/midi/common.h>

static bool running;

static int width;
static int height;
static SDL_Window *window;

void window_open(int req_width, int req_height) {
	width  = req_width;
	height = req_height;

	uint32 window_flags = SDL_WINDOW_OPENGL;
	window = SDL_CreateWindow("OpenGL Test", 0, 0, width, height, window_flags);
	SDL_GLContext context = SDL_GL_CreateContext(window);
}

void window_run() {
	running = true;
	while (running) {
	   glViewport(0, 0, width, height);
	   glClearColor(0.18f, 0.55f, 0.82f, 0.f);
	   glClear(GL_COLOR_BUFFER_BIT);

	   SDL_GL_SwapWindow(window);
	}
}

void window_close() {
	running = false;
}
