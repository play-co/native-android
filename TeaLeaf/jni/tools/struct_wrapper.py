import re
import sys
#TODO allow strings here
type_map = {
    'bool': 'Boolean',
    'int': 'Integer',
    'unsigned int': 'Integer',
    'long': 'Integer',
    'short': 'Integer',
    'char': 'Integer',
    'double': 'Number',
    'float': 'Number',
    'char*': 'String',
    'char *': 'String',
    'rgba': 'String'
}

def gen_struct(name, props):
    src = ''
    for prop in props:
        src += '\t' + prop['type'] + ' ' + prop['name'] + ';\n'
    return src

def gen_getter(name, prop):
    if prop.get('customGetter', None):
        return ''
    fmt = {
            'name': name,
            'prop_type': prop['type'],
            'prop_name': prop['name'], 
            'type_name': type_map[prop['type']]
    }

    if prop['type'] == 'rgba':
        fmt['get_prop'] = """
            char prop[RGBA_MAX_STR_LEN];
            rgba_to_string(&obj->%(prop_name)s, prop);
        """ % fmt
    else:
        fmt['get_prop'] = "%(prop_type)s prop = obj->%(prop_name)s;" % fmt

    return """
v8::Handle<v8::Value> %(name)s_get_%(prop_name)s (v8::Local<String> property, const v8::AccessorInfo &info) {
    v8::Local<v8::Object> thiz = info.Holder();
    %(name)s *obj = (%(name)s*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
    %(get_prop)s;
    return v8::%(type_name)s::New(prop);
}""" % fmt

def gen_setter(name, prop):
    if prop.get('customSetter', None):
        return ''
    js_type = type_map[prop['type']]
    fmt = {
            'name': name, 
            'prop_name': prop['name'], 
            'type_name': js_type
    }

    if js_type == 'String':
        if prop['type'] == 'rgba':
            fmt['store'] = 'rgba_parse(&obj->%(prop_name)s, str);' % fmt
        else:
            # warning: must free this if it changes
            fmt['store'] = """
                if (obj->%(prop_name)s) {
                    free(obj->%(prop_name)s);
                }
                obj->%(prop_name)s = strdup(str);
            """ % fmt
        
        fmt['set'] = """
            if (!value->IsString()) {
                return;
            }

            v8::String::Utf8Value js_str(value);
            const char *str = ToCString(js_str);
            %(store)s;
        """ % fmt
    else:
        fmt['set'] = 'obj->%(prop_name)s = value->To%(type_name)s()->Value();' % fmt
    
    return """
void %(name)s_set_%(prop_name)s (v8::Local<String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
    v8::Local<v8::Object> thiz = info.Holder();
    %(name)s *obj = (%(name)s*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
    %(set)s
}""" % fmt

all_caps = re.compile(r'[A-Z]+')
def _from_camel_case(match):
    value = match.group(0).lower()
    if len(value) > 1:
        value = value[0:-1] + '_' + value[-1]
    
    if match.start(0) == 0:
        return value
    else:
        return '_' + value

def from_camel_case(str):
    return all_caps.sub(_from_camel_case, str)

def gen_method_wrappers(name, methods):
    src = ''
    for method in methods:
        c_method_name = name + '_' + from_camel_case(method)

        src += '    v8::Handle<v8::Value> %s(const v8::Arguments &args);\n' % c_method_name
        src += '    proto_templ->Set(String::New("%s"), v8::FunctionTemplate::New(%s));\n' %  (method, c_method_name)
    return src


def gen_get_set(name, prop):
    if not prop['type'] in type_map:
        return ''
    return '    instance_templ->SetAccessor(v8::String::New("%s"), %s_get_%s, %s_set_%s);\n' % \
        (prop['js_name'], name, prop['name'], name, prop['name'])


def gen_wrapper(name, props, wrapper_name, methods, field_count, constructor):
    getters_setters = ''.join([gen_get_set(name, prop) for prop in props])
    constructor_line = 'v8::Handle<v8::Value> %(constructor)s(const v8::Arguments &args);\n\ttempl->SetCallHandler(%(constructor)s);\n' % {'constructor':constructor} if constructor else ''

    src = """
v8::Handle<v8::FunctionTemplate> get_%(name)s_class_template() {
    v8::Handle<v8::FunctionTemplate> templ = v8::FunctionTemplate::New();
    v8::Handle<v8::ObjectTemplate> instance_templ = templ->InstanceTemplate();
    instance_templ->SetInternalFieldCount(%(field_count)s);
    v8::Handle<v8::ObjectTemplate> proto_templ = templ->PrototypeTemplate();
    %(constructor)s
%(getters_setters)s
%(methods)s
    return templ;
}""" % {'name': name, 'getters_setters': getters_setters, 'methods': gen_method_wrappers(wrapper_name, methods) , "field_count": field_count, "constructor": constructor_line}
    return src


def gen_headers(desc, headers):

    c_headers = desc.get('c_headers', [])
    if 'core/rgba.h' not in c_headers:
        c_headers.append('core/rgba.h')

    return _gen_headers(desc.get('headers', [])) + '\n' + \
        'extern "C" {\n' + _gen_headers(c_headers) + '\n}'

def _gen_headers(headers):
    return '\n'.join(['#include "%s"' % (header_name, ) for header_name in headers])

def gen(name, props, wrapper_name, methods, field_count, constructor):
    src = ''
    for prop in props:
        if 'js_name' not in prop:
            prop['js_name'] = prop['name']
            prop['name'] = from_camel_case(prop['name'])
        if not prop['type'] in type_map:
            print >> sys.stderr, 'Warning: unsupported type', prop['type'], 'getter and setter not defined'
            continue
        src += '\n\n' + gen_getter(name, prop) + '\n' + gen_setter(name, prop)
    src += '\n\n' + gen_wrapper(name, props, wrapper_name, methods, field_count, constructor)
    return src

def wrap_file(name, filename, contents):
    upper_name = name.upper()
    src = '#ifndef %s\n#define %s\n' % (upper_name, upper_name)
    src += '/*\n * WARNING DO NOT EDIT.  THIS FILE AUTOGENERATED FROM ' + filename + '\n *\n */\n\n' + contents + '\n#endif\n'
    return src

if __name__ == '__main__':
    import sys
    import json
    filename = sys.argv[1]
    desc = json.load(open(filename))
    name = desc.get('name', None)
    props = desc.get('props', [])
    wrapper_name = desc.get('wrapperName', None)
    methods = desc.get('methods', [])
    field_count = desc.get('internalFieldCount', 1)
    constructor = desc.get('constructor', None)
    error = False
    if not name:
        error = True
        print 'Missing name'
    if not wrapper_name:
        error = True
        print 'Missing wrapperName'
    if error:
        print 'ERROR'
        sys.exit(1)
    #struct_def = wrap_file(name, sys.argv[1], gen_struct(name, desc['props']))
    headers = gen_headers(desc, 'headers')
    contents = headers + gen(name, props, wrapper_name, methods, field_count, constructor)
    wrapper_def = wrap_file(name + '_wrapper', filename, contents)
    #struct_header_file = open(sys.argv[1].split('.')[0] + '.agh', 'w')
    #struct_header_file.write(struct_def)
    #struct_header_file.close()
    
    out_filename = filename.split('.')[0] + '_wrapper.agh'
    wrapper_file = open(out_filename, 'w')
    wrapper_file.write(wrapper_def)
    wrapper_file.close()
    print 'wrote', out_filename

