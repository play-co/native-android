/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
 */
#include "js/js_context.h"

extern "C" {
#include "core/texture_2d.h"
#include "core/texture_manager.h"
#include "core/tealeaf_canvas.h"
#include "core/tealeaf_context.h"
#include "core/rgba.h"
#include "core/draw_textures.h"
#include "core/log.h"
}
#include "platform/text_manager.h"
#include <string.h>
#include <math.h>
#include <stdlib.h>

using namespace v8;
//extern void print_model_view(context_2d*, int);

Handle<Value> defLoadIdentity(const Arguments& args) {
    HandleScope handleScope;

    context_2d_loadIdentity(GET_CONTEXT2D());
    return Undefined();
}

Handle<Value> defDrawImage(const Arguments& args) {
    LOGFN("drawImage");
    HandleScope handleScope;
    int srcTex = args[0]->Int32Value();
    String::Utf8Value str(args[1]);
    const char *url = ToCString(str);
    float srcX = args[2]->NumberValue();
    float srcY = args[3]->NumberValue();
    float srcW = args[4]->NumberValue();
    float srcH = args[5]->NumberValue();
    float destX = args[6]->NumberValue();
    float destY = args[7]->NumberValue();
    float destW = args[8]->NumberValue();
    float destH = args[9]->NumberValue();

    rect_2d src_rect = {srcX, srcY, srcW, srcH};
    rect_2d dest_rect = {destX, destY, destW, destH};

    context_2d_drawImage(GET_CONTEXT2D(), srcTex, url, &src_rect, &dest_rect);
    LOGFN("endDrawImage");
    return Undefined();
}

Handle<Value> defDrawPointSprites(const Arguments& args) {
    HandleScope handleScope;
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    float point_size = args[1]->NumberValue();
    float step_size = args[2]->NumberValue();

    String::Utf8Value str_color(args[3]);
    rgba color;
    rgba_parse(&color, ToCString(str_color));

    float x1 = args[4]->NumberValue();
    float y1 = args[5]->NumberValue();
    float x2 = args[6]->NumberValue();
    float y2 = args[7]->NumberValue();

    context_2d_draw_point_sprites(GET_CONTEXT2D(), url, point_size, step_size, &color, x1, y1, x2, y2);
    return Undefined();
}

Handle<Value> defDestroyImage(const Arguments& args) {
    LOGFN("destroyImage");
    HandleScope handleScope;
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    //call acquire here because we need to lock the mutex
    texture_2d *tex = texture_manager_get_texture(texture_manager_acquire(), url);
    if (tex && tex->loaded) {
        texture_manager_free_texture(texture_manager_get(), tex);
    }
    texture_manager_release();
    LOGFN("endDestroyImage");
    return Undefined();
}

Handle<Value> defRotate(const Arguments& args) {
    LOGFN("rotate");
    HandleScope handleScope;
    double angle = args[0]->NumberValue();

    context_2d_rotate(GET_CONTEXT2D(), angle);
    LOGFN("endrotate");
    return Undefined();

}

Handle<Value> defTranslate(const Arguments& args) {
    LOGFN("translate");
    HandleScope handleScope;
    double x = args[0]->NumberValue();
    double y = args[1]->NumberValue();

    context_2d_translate(GET_CONTEXT2D(), x, y);

    LOGFN("endtranslate");
    return Undefined();
}

Handle<Value> defScale(const Arguments& args) {
    LOGFN("scale");
    HandleScope handleScope;
    double x = args[0]->NumberValue();
    double y = args[1]->NumberValue();

    context_2d_scale(GET_CONTEXT2D(), x, y);

    LOGFN("endscale");
    return Undefined();

}

Handle<Value> defSave(const Arguments& args) {
    LOGFN("save");
    HandleScope handleScope;
    context_2d_save(GET_CONTEXT2D());
    LOGFN("endsave");
    return Undefined();
}

Handle<Value> defRestore(const Arguments& args) {
    LOGFN("restore");
    HandleScope handleScope;
    context_2d_restore(GET_CONTEXT2D());
    LOGFN("endrestore");
    return Undefined();
}

Handle<Value> defClear(const Arguments& args) {
    LOGFN("clear");
    HandleScope handleScope;
    context_2d_clear(GET_CONTEXT2D());
    LOGFN("endclear");
    return Undefined();
}

Handle<Value> defSetGlobalAlpha(const Arguments& args) {
    LOGFN("setglobalalpha");
    HandleScope handleScope;
    double alpha = args[0]->NumberValue();
    context_2d_setGlobalAlpha(GET_CONTEXT2D(), alpha);
    LOGFN("endsetglobalalpha");
    return Undefined();
}

Handle<Value> defGetGlobalAlpha(const Arguments& args) {
    LOGFN("getglobalalpha");
    HandleScope handleScope;
    double alpha = context_2d_getGlobalAlpha(GET_CONTEXT2D());

    LOGFN("endgetglobalalpha");
    return Number::New(alpha);
}

Handle<Value> defLoadImage(const Arguments& args) {
    LOGFN("loadImage");
    HandleScope handleScope;

    String::Utf8Value str(args[0]);
    char *url =(char*) ToCString(str);

    texture_2d *tex = texture_manager_load_texture(texture_manager_get(), url);
    if (!tex || !tex->loaded) {
        return False();
    }

    Local<Object> ret(Object::New());

    ret->Set(STRING_CACHE_width, Integer::New(tex->originalWidth));
    ret->Set(STRING_CACHE_height, Integer::New(tex->originalHeight));
    ret->Set(STRING_CACHE_name, Integer::New(tex->name));

    LOGFN("endloadImage");
    return handleScope.Close(ret);
}

Handle<Value> defClearRect(const Arguments& args) {
    LOGFN("clearRect");
    double x = args[0]->NumberValue();
    double y = args[1]->NumberValue();
    double width = args[2]->NumberValue();
    double height = args[3]->NumberValue();

    rect_2d rect = {x, y, width, height};

    context_2d_clearRect(GET_CONTEXT2D(), &rect);

    LOGFN("endclearRect");
    return Undefined();
}

Handle<Value> defFillRect(const Arguments& args) {
    double x = args[0]->NumberValue();
    double y = args[1]->NumberValue();
    double width = args[2]->NumberValue();
    double height = args[3]->NumberValue();

    String::Utf8Value str_color(args[4]);
    rgba color;
    rgba_parse(&color, ToCString(str_color));

    rect_2d rect = {x, y, width, height};

    context_2d_fillRect(GET_CONTEXT2D(), &rect, &color);

    return Undefined();
}

Handle<Value> defStrokeRect(const Arguments& args) {
    double x = args[0]->NumberValue();
    double y = args[1]->NumberValue();
    double width = args[2]->NumberValue();
    double height = args[3]->NumberValue();
    context_2d *ctx = GET_CONTEXT2D();

    String::Utf8Value str_color(args[4]);
    rgba color;
    rgba_parse(&color, ToCString(str_color));

    double line_width1 = args[5]->Int32Value();
    double line_width2 = line_width1 / 2;

    rect_2d left_rect = {x - line_width2, y - line_width2, line_width1, height + line_width1};
    context_2d_fillRect(ctx, &left_rect, &color);

    rect_2d right_rect = {x + width - line_width2, y - line_width2, line_width1, height + line_width1};
    context_2d_fillRect(ctx, &right_rect, &color);

    rect_2d top_rect = {x + line_width2, y - line_width2, width - line_width1, line_width1};
    context_2d_fillRect(ctx, &top_rect, &color);

    rect_2d bottom_rect = {x + line_width2, y + height - line_width2, width - line_width1, line_width1};
    context_2d_fillRect(ctx, &bottom_rect, &color);

    return Undefined();
}

#define FONT_SCALE	0.9
Handle<Value> defMeasureText(const Arguments& args) {
    LOGFN("measuretext");
    HandleScope handle_scope;
    String::Utf8Value text_str(args[0]);
    const char* text = ToCString(text_str);
    int size = args[1]->Int32Value();
    String::Utf8Value font_str(args[2]);
    const char *font = ToCString(font_str);

    int width = text_manager_measure_text(font, size * FONT_SCALE, text);
    Handle<Object> metrics =  Object::New();
    metrics->Set(STRING_CACHE_width, Number::New(width));

    LOGFN("endmeasuretext");
    return handle_scope.Close(metrics);
}

double measureText(Handle<Object> font_info, char **text) {
    double width = 0;

    Handle<Object> custom_font = Handle<Object>::Cast(font_info->Get(STRING_CACHE_customFont));
    if (custom_font.IsEmpty()) {
        return 0;
    }

    Handle<Object> dimensions = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_dimensions));
    if (dimensions.IsEmpty()) {
        return 0;
    }

    Handle<Object> horizontal = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_horizontal));

    float scale = font_info->Get(STRING_CACHE_scale)->NumberValue();
    float space_width = horizontal->Get(STRING_CACHE_width)->NumberValue() * scale;
    float tab_width = 4 * space_width;
    Handle<Object> settings = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_settings));
    float spacing = settings->Get(STRING_CACHE_spacing)->NumberValue() * scale;

    char c = '\0';
    for (int i = 0; (c = (*text)[i]) != 0; i++) {
        if (c == ' ') {
            width += space_width;
        } else if (c == '\t') {
            width += tab_width;
        } else {
            Handle<Object> dimension = Handle<Object>::Cast(dimensions->Get(Number::New((int)c)));
            if (!dimension.IsEmpty() && dimension->IsObject()) {
                int xadvance = dimension->Get(STRING_CACHE_xadvance)->Int32Value();
                width += xadvance * scale;
            } else {
                return -1;
            }
        }
        width += spacing;
    }

    return width;
}

Handle<Value> defMeasureTextBitmap(const Arguments &args) {
    HandleScope handle_scope;

    String::Utf8Value text_str(args[0]);
    const char *text = ToCString(text_str);
    Handle<Object> font_info = args[1]->ToObject();
    double width = measureText(font_info, (char**)&text);

    Handle<Object> metrics = Object::New();
    metrics->Set(STRING_CACHE_width, Number::New(width));
    metrics->Set(STRING_CACHE_failed, Boolean::New(width < 0));

    return handle_scope.Close(metrics);
}

double textBaselineValue(Handle<Object> ctx, Handle<Object> custom_font, double scale) {
    Handle<String> text_baseline = ctx->Get(STRING_CACHE_textBaseline)->ToString();
    if (!text_baseline.IsEmpty()) {
        String::Utf8Value text_baseline_str(text_baseline);
        const char *baseline = ToCString(text_baseline_str);
        Handle<Object> vertical;
        double b;

        if (!strcmp(baseline, "alphabetic")) {
            vertical = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_vertical));
            b = vertical->Get(STRING_CACHE_baseline)->NumberValue();
            return -b * scale;
        } else if (!strcmp(baseline, "middle")) {
            vertical = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_vertical));
            b = vertical->Get(STRING_CACHE_bottom)->NumberValue();
            return -b / 2 * scale;
        } else if (!strcmp(baseline, "bottom")) {
            vertical = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_vertical));
            b = vertical->Get(STRING_CACHE_bottom)->NumberValue();
            return -b * scale;
        }
    }

    return 0;
}

double textAlignValue(Handle<Object> ctx, Handle<Object> font_info, char **text) {
    Handle<String> text_align = ctx->Get(STRING_CACHE_textAlign)->ToString();
    if (!text_align.IsEmpty()) {
        String::Utf8Value text_align_str(text_align);
        const char *align = ToCString(text_align_str);
        if (!strcmp(align, "center")) {
            return -measureText(font_info, text) / 2;
        } else if (!strcmp(align, "right")) {
            return -measureText(font_info, text);
        }
    }

    return 0;
}

Handle<Value> defFillTextBitmap(const Arguments &args) {
    Handle<Object> ctx = Handle<Object>::Cast(args[0]);
    context_2d *context = GET_CONTEXT2D();
    double x = args[1]->NumberValue();
    double y = args[2]->NumberValue();
    String::Utf8Value text_str(args[3]);
    double max_width = args[4]->NumberValue();
    const char *text = ToCString(text_str);
    // args[5] is color, done by the filter
    Handle<Object> font_info = args[6]->ToObject();
    Handle<Object> custom_font = Handle<Object>::Cast(font_info->Get(STRING_CACHE_customFont));
    Handle<Object> images;
    int is_stroke = args[7]->Int32Value();
    if (is_stroke == 1) {
        images = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_strokeImages));
    } else {
        images = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_images));
    }
    Handle<Object> dimensions = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_dimensions));
    Handle<Object> horizontal = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_horizontal));

    double width = measureText(font_info, (char**)&text);

    float scale = 1;
    Handle<Value> scale_value = font_info->Get(STRING_CACHE_scale);
    if (!scale_value.IsEmpty()) {
        scale = scale_value->NumberValue();
    }

    if (width > max_width && width > 0) {
        scale *= max_width / width;
    }

    float space_width = 4;
    Handle<Value> space_width_value = horizontal->Get(STRING_CACHE_width);
    if (!space_width_value.IsEmpty()) {
        space_width = space_width_value->NumberValue() * scale;
    }
    float tab_width = 4 * space_width;

    float spacing = 0;
    Handle<Object> settings = Handle<Object>::Cast(custom_font->Get(STRING_CACHE_settings));
    Handle<Value> spacing_value = settings->Get(STRING_CACHE_spacing);
    if (!spacing_value.IsEmpty()) {
        spacing = spacing_value->NumberValue() * scale;
    }

    y += textBaselineValue(ctx, custom_font, scale);
    x += textAlignValue(ctx, font_info, (char**)&text);

    int current_sheet_index = -1;
    Handle<Object> image;
    Handle<String> src_tex;
    char *url = NULL;
    char c = '\0';
    for (int i = 0; (c = text[i]) != 0; i++) {
        if (c == ' ') {
            x += space_width + spacing;
        } else if (c == '\t') {
            x += tab_width + spacing;
        } else {
            Handle<Object> dimension = Handle<Object>::Cast(dimensions->Get(Number::New((int)c)));
            if (!dimension.IsEmpty() && dimension->IsObject()) {
                int sheet_index = dimension->Get(STRING_CACHE_sheetIndex)->Int32Value();
                int sx = dimension->Get(STRING_CACHE_x)->Int32Value();
                int sy = dimension->Get(STRING_CACHE_y)->Int32Value();
                int sw = dimension->Get(STRING_CACHE_w)->Int32Value();
                int sh = dimension->Get(STRING_CACHE_h)->Int32Value();
                int ox = dimension->Get(STRING_CACHE_ox)->Int32Value();
                int oy = dimension->Get(STRING_CACHE_oy)->Int32Value();
                int xadvance = dimension->Get(STRING_CACHE_xadvance)->Int32Value();

                rect_2d src_rect = {sx, sy, sw, sh};
                rect_2d dest_rect = {x + ox * scale, y + oy * scale, sw * scale, sh * scale};

                if (current_sheet_index != sheet_index) {
                    current_sheet_index = sheet_index;
                    image = Handle<Object>::Cast(images->Get(Number::New(sheet_index)));
                    src_tex = image->Get(STRING_CACHE__src)->ToString();
                    free(url);
                    String::Utf8Value src_tex_str(src_tex);
                    url = strdup(ToCString(src_tex_str));
                }

                context_2d_drawImage(context, 0, url, &src_rect, &dest_rect);

                x += xadvance * scale + spacing;
            } else {
                x += space_width + spacing;
            }
        }
    }

    free(url);

    return Boolean::New(true);
}

Handle<Value> defStrokeText(const Arguments& args) {
    LOGFN("stroketext");
    String::Utf8Value text_str(args[0]);
    const char* text = ToCString(text_str);
    int x = args[1]->Int32Value();
    int y = args[2]->Int32Value();
    int max_width = args[3]->Int32Value();

    String::Utf8Value str_color(args[4]);
    rgba color;
    rgba_parse(&color, ToCString(str_color));

    int size = args[5]->Int32Value();
    String::Utf8Value font_str(args[6]);
    const char *font = ToCString(font_str);
    double line_width = args[9]->NumberValue();
    texture_2d *texture = text_manager_get_stroked_text(font, size * FONT_SCALE, text, &color, max_width, (float)line_width);

    if (texture) {
        String::Utf8Value str(args[7]);
        const char *align = ToCString(str);

        int x_offset = 0;
        int y_offset = 0;
        if (!strcmp(align, "left")) {
            x_offset = 0;
        } else if (!strcmp(align, "right")) {
            x_offset = texture->originalWidth;
        } else if (!strcmp(align, "center")) {
            x_offset = texture->originalWidth/2;
        }

        String::Utf8Value str2(args[8]);
        const char *baseline = ToCString(str2);
        if (!strcmp(baseline, "bottom")) {
            y_offset = texture->originalHeight;
        } else if (!strcmp(baseline, "middle")) {
            y_offset = texture->originalHeight/2;
        } else { // top
            y_offset = 0;
        }
        rect_2d src_rect = {0, 0, texture->originalWidth, texture->originalHeight};
        rect_2d dest_rect = {x - x_offset - (int)line_width, y - y_offset, texture->originalWidth, texture->originalHeight};
        context_2d_fillText(GET_CONTEXT2D(), texture, &src_rect, &dest_rect, color.a);
    }
    LOGFN("endstroketext");
    return Undefined();
}

Handle<Value> defFillText(const Arguments& args) {
    LOGFN("filltext");
    String::Utf8Value text_str(args[0]);
    const char* text = ToCString(text_str);
    int x = args[1]->Int32Value();
    int y = args[2]->Int32Value();
    int max_width = args[3]->Int32Value();

    String::Utf8Value str_color(args[4]);
    rgba color;
    rgba_parse(&color, ToCString(str_color));

    int size = args[5]->Int32Value();
    String::Utf8Value font_str(args[6]);
    const char *font = ToCString(font_str);
    texture_2d *texture = text_manager_get_filled_text(font, size * FONT_SCALE, text, &color, max_width);

    if (texture) {
        String::Utf8Value str(args[7]);
        const char *align = ToCString(str);

        int x_offset = 0;
        int y_offset = 0;
        if (!strcmp(align, "left")) {
            x_offset = 0;
        } else if (!strcmp(align, "right")) {
            x_offset = texture->originalWidth;
        } else if (!strcmp(align, "center")) {
            x_offset = texture->originalWidth/2;
        }

        String::Utf8Value str2(args[8]);
        const char *baseline = ToCString(str2);
        if (!strcmp(baseline, "bottom")) {
            y_offset = texture->originalHeight;
        } else if (!strcmp(baseline, "middle")) {
            y_offset = texture->originalHeight/2;
        } else { // top
            y_offset = 0;
        }
        rect_2d src_rect = {0, 0, texture->originalWidth, texture->originalHeight};
        rect_2d dest_rect = {x - x_offset, y - y_offset, texture->originalWidth, texture->originalHeight};
        context_2d_fillText(GET_CONTEXT2D(), texture, &src_rect, &dest_rect, color.a);
    }
    LOGFN("endfilltext");
    return Undefined();
}

Handle<Value> defFlushImages(const Arguments& args) {
    LOGFN("flushImages");
    draw_textures_flush();
    LOGFN("endflushImages");
    return Undefined();
}

Handle<Value> defNewTexture(const Arguments& args) {
    LOGFN("newTexture");
    HandleScope handleScope;
    int w = args[0]->Int32Value();
    int h = args[1]->Int32Value();

    texture_2d *tex = texture_manager_new_texture(texture_manager_get(), w, h);

    Handle<Object> tex_data = Object::New();
    tex_data->Set(STRING_CACHE___gl_name, Number::New(tex->name));
    tex_data->Set(STRING_CACHE__src, String::New(tex->url));
    LOGFN("endnewTexture");
    return tex_data;
}

Handle<Value> defEnableScissor(const Arguments& args) {
    LOGFN("enableScissor");
    double x = args[0]->NumberValue();
    double y = args[1]->NumberValue();
    double width = args[2]->NumberValue();
    double height = args[3]->NumberValue();
    rect_2d bounds = {x, y, width, height};
    context_2d_setClip(GET_CONTEXT2D(), bounds);
    LOGFN("endenableScissor");
    return Undefined();
}

Handle<Value> defDisableScissor(const Arguments& args) {
    disable_scissor(GET_CONTEXT2D());
    return Undefined();
}

Handle<Value> defAddFilter(const Arguments &args) {
    LOGFN("addFilter");

    Handle<Value> filter = args[1];
    if (filter.IsEmpty() || !filter->IsObject()) {
        LOG("{context} WARNING: Invalid filter provided");
    } else {
        Handle<Object> filter_object = filter->ToObject();

        String::Utf8Value type_str(filter_object->Get(STRING_CACHE_type));
        const char *type = ToCString(type_str);
        if(strncmp(type,"LinearAdd",strlen("LinearAdd"))==0) {
            context_2d_set_filter_type(GET_CONTEXT2D(), FILTER_LINEAR_ADD);
        } else if(strncmp(type,"Multiply",strlen("Multiply"))==0) {
            context_2d_set_filter_type(GET_CONTEXT2D(), FILTER_MULTIPLY);
        }

        double r = filter_object->Get(STRING_CACHE_r)->NumberValue();
        double g = filter_object->Get(STRING_CACHE_g)->NumberValue();
        double b = filter_object->Get(STRING_CACHE_b)->NumberValue();
        double a = filter_object->Get(STRING_CACHE_a)->NumberValue();
        //convert the 0-255 values to floats
        r /= 255;
        g /= 255;
        b /= 255;
        rgba color = {r, g, b, a};
        context_2d_add_filter(GET_CONTEXT2D(), &color);
    }
    return Undefined();
}

Handle<Value> defClearFilters(const Arguments &args) {
    GET_CONTEXT2D()->filter_type = FILTER_NONE;
    context_2d_clear_filters(GET_CONTEXT2D());
    return Undefined();
}

Handle<ObjectTemplate> get_context_2d_class_template();

static void context_2d_class_finalize(Persistent<Value> ctx, void *param) {
    HandleScope handle_scope;

    LOGFN("ctx2d dtor");

    context_2d *_ctx = static_cast<context_2d*>( param );
    context_2d_delete(_ctx);

    int size = _ctx->backing_width * _ctx->backing_height * 4;
    V8::AdjustAmountOfExternalAllocatedMemory(-size);

    ctx.Dispose();
    ctx.Clear();

    LOGFN("endctx2d dtor");
}

Handle<Value> context_2d_class_ctor(const Arguments& args) {
    LOGFN("ctx2d ctor");
    Handle<Object> canvas = args[0]->ToObject();
    String::Utf8Value str(args[1]);
    const char *url = ToCString(str);
    int destTex = args[2]->Int32Value();
    Persistent<Object> ctx = Persistent<Object>::New(get_context_2d_class_template()->NewInstance());
    ctx->Set(STRING_CACHE_canvas, canvas);
    context_2d *_ctx = context_2d_new(tealeaf_canvas_get(), url, destTex);
    ctx->SetInternalField(0, External::New(_ctx));

    //now make it weak
    ctx.MakeWeak(_ctx, context_2d_class_finalize);

    int size = _ctx->backing_width * _ctx->backing_height * 4;
    V8::AdjustAmountOfExternalAllocatedMemory(size);

    LOGFN("endctx2d ctor");
    return ctx;
}

Handle<Value> js_gl_delete_textures(const Arguments& args) {
    LOGFN("start js_gl_delete_textures");
    texture_manager_clear_textures(texture_manager_get(), true);
    LOGFN("end js_gl_delete_textures");
    return Undefined();
}

Handle<Value> js_gl_touch_texture(const Arguments& args) {
    LOGFN("end js_gl_touch_texture");
    String::Utf8Value url_str(args[0]);
    const char *url = ToCString(url_str);
    texture_manager_touch_texture(texture_manager_get(), url);
    LOGFN("start js_gl_touch_texture");
    return Undefined();
}

Handle<Value> defResize(const Arguments& args) {
    int width = args[0]->Int32Value();
    int height = args[1]->Int32Value();

    context_2d *ctx = GET_CONTEXT2D();
    context_2d_resize(ctx, width, height);
    // texture may have changed, so return tex info to client js
    texture_2d *tex = texture_manager_get_texture(texture_manager_get(), ctx->url);
    Handle<Object> tex_data = Object::New();
    tex_data->Set(STRING_CACHE___gl_name, Number::New(tex->name));
    tex_data->Set(STRING_CACHE__src, String::New(tex->url));
    return tex_data;
}

Handle<Value> defFillTextBitmapDeprecated(const Arguments &args) {
    String::Utf8Value str(args[0]);
    const char *text = ToCString(str);
    double x = args[1]->NumberValue();
    double y = args[2]->NumberValue();
    float scale = args[3]->NumberValue();
    String::Utf8Value str2(args[4]);
    const char *src_tex = ToCString(str2);
    int tex_name = args[5]->Int32Value();
    Handle<Object> defs = args[6]->ToObject();
    //int composite_op = args[7]->Int32Value();


    int space_width = defs->Get(STRING_CACHE_spaceWidth)->Int32Value();
    char c = '\0';
    char buf[2] = {'\0'};
    for (int i = 0; (c = text[i]) != 0; i++) {
        if (c == ' ') {
            x += space_width * scale;
        } else {
            snprintf(buf, sizeof(buf), "%c", c);
            Handle<Object> def = Handle<Object>::Cast(defs->Get(String::New(buf)));
            if (!def.IsEmpty()) {
                int a = def->Get(STRING_CACHE_a)->Int32Value();
                int c = def->Get(STRING_CACHE_c)->Int32Value();
                int x1 = def->Get(STRING_CACHE_x1)->Int32Value();
                int y1 = def->Get(STRING_CACHE_y1)->Int32Value();
                int w = def->Get(STRING_CACHE_w)->Int32Value();
                int h = def->Get(STRING_CACHE_h)->Int32Value();

                rect_2d src_rect = {x1, y1, w, h};
                rect_2d dest_rect = {x, y, w * scale, h * scale};
                x += a * scale;
                context_2d_drawImage(GET_CONTEXT2D(), tex_name, src_tex, &src_rect, &dest_rect);
                x += c * scale;
            }
        }
    }
    return Undefined();
}

Handle<Value> defSetGlobalCompositeOperation(const Arguments& args) {
    LOGFN("setGlobalCompositeOperation");
    HandleScope handleScope;
    int composite_op = args[0]->Int32Value();
    context_2d_setGlobalCompositeOperation(GET_CONTEXT2D(), composite_op);
    LOGFN("endsetGlobalCompositeOperation");
    return Undefined();
}

Handle<Value> defGetGlobalCompositeOperation(const Arguments& args) {
    LOGFN("getGlobalCompositeOperation");
    HandleScope handleScope;
    int composite_op = context_2d_getGlobalCompositeOperation(GET_CONTEXT2D());

    LOGFN("endgetGlobalCompositeOperation");
    return Number::New(composite_op);
}

/**
   Retrieves the given context

   @param	args[0] => the given context2d, can be connected to an on/off-screen buffer
   @param	args[1] => filename to save the on/off-screen buffer with
   @return 	Undefined
**/
Handle<Value> defSaveBufferToFile(const Arguments& args) {
    //get this context 2d instance
//	Handle<Object> js_ctx = Handle<Object>::Cast(args[0]);
//	Handle<Object> _ctx = Handle<Object>::Cast(js_ctx->Get(String::New("_ctx")));
//	context_2d *ctx = GET_CONTEXT2D_FROM(_ctx);
//	//get filename for the fbo to be saved to
//	String::Utf8Value filename_str(args[1]);
//	const char *filename = ToCString(filename_str);
//	bool did_save = context_2d_save_buffer_to_file(ctx, filename);
    return Boolean::New(true);
}

/**
   Retrieves the given context
   @param	args[0] => the given context2d, can be connected to an on/off-screen buffer
   @return 	Undefined
**/
Handle<Value> defToDataURL(const Arguments& args) {
    //get this context 2d instance
    Handle<Object> js_ctx = Handle<Object>::Cast(args[0]);
    Handle<Object> _ctx = Handle<Object>::Cast(js_ctx->Get(String::New("_ctx")));
    context_2d *ctx = GET_CONTEXT2D_FROM(_ctx);
    char * data = context_2d_save_buffer_to_base64(ctx, "PNG");
    Handle<Value> str;
    if (data != NULL) {
        str = String::New(data);
        free(data);
    } else {
        str = String::New("");
    }
    return str;
}

Handle<Value> defSetTransform(const Arguments& args) {
    double m11 = args[0]->NumberValue();
    double m21 = args[1]->NumberValue();
    double m12 = args[2]->NumberValue();
    double m22 = args[3]->NumberValue();
    double dx = args[4]->NumberValue();
    double dy = args[5]->NumberValue();

    context_2d_setTransform(GET_CONTEXT2D(), m11, m21, m12, m22, dx, dy);

    return Undefined();
}


void js_gl_init() {
}

Handle<ObjectTemplate> get_context_2d_class_template() {
    Handle<ObjectTemplate> context_2d_class_template;
    context_2d_class_template = ObjectTemplate::New();
    context_2d_class_template->SetInternalFieldCount(1);

    context_2d_class_template->Set(STRING_CACHE_loadIdentity, FunctionTemplate::New(defLoadIdentity));
    context_2d_class_template->Set(STRING_CACHE_drawImage, FunctionTemplate::New(defDrawImage));
    context_2d_class_template->Set(STRING_CACHE_flushDrawImage, FunctionTemplate::New(defFlushImages));
    context_2d_class_template->Set(STRING_CACHE_newTexture, FunctionTemplate::New(defNewTexture));
    context_2d_class_template->Set(STRING_CACHE_rotate, FunctionTemplate::New(defRotate));
    context_2d_class_template->Set(STRING_CACHE_scale, FunctionTemplate::New(defScale));
    context_2d_class_template->Set(STRING_CACHE_translate, FunctionTemplate::New(defTranslate));
    context_2d_class_template->Set(STRING_CACHE_save, FunctionTemplate::New(defSave));
    context_2d_class_template->Set(STRING_CACHE_restore, FunctionTemplate::New(defRestore));
    context_2d_class_template->Set(STRING_CACHE_clear, FunctionTemplate::New(defClear));
    context_2d_class_template->Set(STRING_CACHE_setGlobalAlpha, FunctionTemplate::New(defSetGlobalAlpha));
    context_2d_class_template->Set(STRING_CACHE_getGlobalAlpha, FunctionTemplate::New(defGetGlobalAlpha));
    context_2d_class_template->Set(STRING_CACHE__loadImage, FunctionTemplate::New(defLoadImage));
    context_2d_class_template->Set(STRING_CACHE_clearRect, FunctionTemplate::New(defClearRect));
    context_2d_class_template->Set(STRING_CACHE_fillRect, FunctionTemplate::New(defFillRect));
    context_2d_class_template->Set(STRING_CACHE_strokeRect, FunctionTemplate::New(defStrokeRect));
    context_2d_class_template->Set(STRING_CACHE_measureText, FunctionTemplate::New(defMeasureText));
    context_2d_class_template->Set(STRING_CACHE_fillText, FunctionTemplate::New(defFillText));
    context_2d_class_template->Set(STRING_CACHE_strokeText, FunctionTemplate::New(defStrokeText));
    context_2d_class_template->Set(STRING_CACHE_enableScissor, FunctionTemplate::New(defEnableScissor));
    context_2d_class_template->Set(STRING_CACHE_disableScissor, FunctionTemplate::New(defDisableScissor));
    context_2d_class_template->Set(STRING_CACHE_drawPointSprites, FunctionTemplate::New(defDrawPointSprites));
    context_2d_class_template->Set(String::New("setGlobalCompositeOperation"), FunctionTemplate::New(defSetGlobalCompositeOperation));
    context_2d_class_template->Set(String::New("getGlobalCompositeOperation"), FunctionTemplate::New(defGetGlobalCompositeOperation));
    context_2d_class_template->Set(String::New("setTransform"), FunctionTemplate::New(defSetTransform));
    context_2d_class_template->Set(STRING_CACHE_resize, FunctionTemplate::New(defResize));

    // bitmap fonts
    context_2d_class_template->Set(STRING_CACHE_measureTextBitmap, FunctionTemplate::New(defMeasureTextBitmap));
    context_2d_class_template->Set(STRING_CACHE_fillTextBitmap, FunctionTemplate::New(defFillTextBitmap));

    // deprecated
    context_2d_class_template->Set(STRING_CACHE_fillTextBitmapDeprecated, FunctionTemplate::New(defFillTextBitmapDeprecated));

    context_2d_class_template->Set(STRING_CACHE_addFilter, FunctionTemplate::New(defAddFilter));
    context_2d_class_template->Set(STRING_CACHE_clearFilters, FunctionTemplate::New(defClearFilters));

    return context_2d_class_template;
}

Handle<ObjectTemplate> js_gl_get_template() {
    Handle<ObjectTemplate> gl = ObjectTemplate::New();
    gl->Set(String::New("toDataURL"), FunctionTemplate::New(defToDataURL));
    gl->Set(STRING_CACHE_Context2D, FunctionTemplate::New(context_2d_class_ctor));
    gl->Set(STRING_CACHE_flushImages, FunctionTemplate::New(defFlushImages));
    gl->Set(STRING_CACHE__loadImage, FunctionTemplate::New(defLoadImage));
    gl->Set(STRING_CACHE_newTexture, FunctionTemplate::New(defNewTexture));
    gl->Set(STRING_CACHE_deleteTexture, FunctionTemplate::New(defDestroyImage));
    gl->Set(STRING_CACHE__fillText, FunctionTemplate::New(defFillTextBitmap));
    gl->Set(STRING_CACHE_deleteAllTextures, FunctionTemplate::New(js_gl_delete_textures));
    gl->Set(STRING_CACHE_touchTexture, FunctionTemplate::New(js_gl_touch_texture));

    return gl;
}
