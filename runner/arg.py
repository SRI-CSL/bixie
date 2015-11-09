import argparse
import os
import sys
import imp

# token that identifies the end of the options for infer and the beginning
# of the compilation command
CMD_MARKER = '--'

# insert here the correspondence between module name and the list of
# compiler/build-systems it handles.
# All supported commands should be listed here
MODULE_TO_COMMAND = {
    'ant': ['ant'],
    'gradle': ['gradle', 'gradlew'],
    'mvn': ['mvn']
}

CAPTURE_PACKAGE = 'capture'
DEFAULT_JAR_LOCATION = os.path.abspath(
  os.path.join (os.path.dirname(__file__),
                os.pardir,
                'build',
                'libs',
                'bixie.jar'))

class AbsolutePathAction(argparse.Action):
    """Convert a path from relative to absolute in the arg parser"""
    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, os.path.abspath(values))

base_parser = argparse.ArgumentParser(add_help=False)
base_group = base_parser.add_argument_group('global arguments')
base_group.add_argument('-o', '--out', metavar='<directory>',
                        dest='output_directory',
                        action=AbsolutePathAction,
                        help='Set the results directory')
base_group.add_argument('--jar', action=AbsolutePathAction,
                        help='The location of the bixie jar file.',
                        default=DEFAULT_JAR_LOCATION)

base_group.add_argument(
    CMD_MARKER,
    metavar='<cmd>',
    dest='nullarg',
    default=None,
    help=('Command to run the compiler/build-system.'),
)

def get_module_name(command):
    """ Return module that is able to handle the command. None if
    there is no such module."""
    for module, commands in MODULE_TO_COMMAND.iteritems():
        if command in commands:
            return module
    return None

def split_args_to_parse():
    dd_index = \
        sys.argv.index(CMD_MARKER) if CMD_MARKER in sys.argv else len(sys.argv)

    args, cmd = sys.argv[1:dd_index], sys.argv[dd_index + 1:]
    capture_module_name = os.path.basename(cmd[0]) if len(cmd) > 0 else None
    mod_name = get_module_name(capture_module_name)
    return args, cmd, mod_name

def load_module(mod_name):
    # load the 'capture' package in lib
    pkg_info = imp.find_module(CAPTURE_PACKAGE)
    imported_pkg = imp.load_module(CAPTURE_PACKAGE, *pkg_info)
    # load the requested module (e.g. make)
    mod_file, mod_path, mod_descr = \
        imp.find_module(mod_name, imported_pkg.__path__)
    try:
        return imp.load_module(
            '{pkg}.{mod}'.format(pkg=imported_pkg.__name__, mod=mod_name),
            mod_file, mod_path, mod_descr)
    finally:
        if mod_file:
            mod_file.close()

def parse_args():
    to_parse, cmd, mod_name = split_args_to_parse()
    # get the module name (if any), then load it
    imported_module = None
    if mod_name:
        imported_module = load_module(mod_name)

    args = base_parser.parse_args(to_parse)

    if imported_module:
        return args, cmd, imported_module
    else:
        base_parser.print_help()
        sys.exit(os.EX_OK)
