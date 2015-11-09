# Copyright (c) 2015 - present Facebook, Inc.
# All rights reserved.
#
# This source code is licensed under the BSD style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.

import re
import util
import generic

def gen_instance(cmd):
    return MavenCapture(cmd)

class MavenCapture(generic.GenericCapture):
    def __init__(self, cmd):
        self.build_cmd = ['mvn', '-X'] + cmd[1:]

    def get_javac_commands(self, verbose_output):
        file_pattern = r'\[DEBUG\] Stale source detected: ([^ ]*\.java)'
        options_pattern = '[DEBUG] Command line options:'

        javac_commands = []
        files_to_compile = []
        options_next = False

        for line in verbose_output:
            if options_next:
                #  line has format [Debug] <space separated options>
                javac_args = line.split(' ')[1:] + files_to_compile
                javac_commands.append(javac_args)
                options_next = False
                files_to_compile = []
            elif options_pattern in line:
                #  Next line will have javac options to run
                options_next = True

            else:
                found = re.match(file_pattern, line)
                if found:
                    files_to_compile.append(found.group(1))

        return map(self.javac_parse, javac_commands)
