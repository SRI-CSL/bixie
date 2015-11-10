import os
import sys
import subprocess
import argparse
import arg

def mkdir(newdir):
  if not os.path.isdir(newdir):
    os.makedirs(newdir)

def analyze_project(args, javac_commands):
  i = 1
  for command in javac_commands:
    switches = command['javac_switches']
    classpath = base_path = switches['d']
    
    if 'cp' in switches:
      classpath = switches['cp']
    if 'classpath' in switches:
      classpath = switches['classpath']

    bixie_command = ['java', '-jar', args.jar,
                     '-html', 'bixie_report_%d' % i,
                     '-j', base_path,
                     '-cp', classpath]

    if 'java_files' in command:
      src_string = (os.pathsep).join(command['java_files'])
      bixie_command.extend(['-src', src_string])

    if args.output_directory:
      out_filename = os.path.join(args.output_directory, "report-%d.log" % i)
      bixie_command += ['-o', out_filename]

    subprocess.call(bixie_command)
    i += 1

def main():
  args, cmd, imported_module = arg.parse_args()

  javac_commands = imported_module.gen_instance(cmd).capture();

  if args.output_directory:
    mkdir(args.output_directory)

  analyze_project(args, javac_commands)

if __name__ == "__main__":
  main()
