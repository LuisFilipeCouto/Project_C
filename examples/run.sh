# Check if a file argument is provided
if [ $# -eq 0 ]; then
  echo "Please provide a python filename as an argument"
  exit 1
fi

# Change directory to src
cd bin || {
    echo "Error: Could not change directory to bin"
    exit 1
}

# Check if the file exists
filename=$1
if [ ! -f "$1" ]; then
  echo "The file '$1' does not exist"
  exit 1
fi

# Execute command and check for failure
execute_command() {
    if ! "$@" > /dev/null 2>&1; then
        echo "Error: Command '$@' failed"
        exit 1
    fi
}

# Commands
python3 $filename
