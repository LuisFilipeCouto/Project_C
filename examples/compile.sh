# Check if a file argument is provided
if [ $# -eq 0 ]; then
  echo "Please provide a filename as an argument."
  exit 1
fi

# Check if the file exists
filename=$1
if [ ! -f "$1" ]; then
  echo "The file '$1' does not exist."
  exit 1
fi

# Create bin folder if it doesn't exist
directory="bin"
if [ ! -d "$directory" ]; then
    mkdir "$directory"
fi

# Change directory to src
cd ../src || {
    echo "Error: Could not change directory to ../src"
    exit 1
}

# Execute commands and check for failure
execute_command() {
    if ! "$@" > /dev/null 2>&1; then
        echo "Error: Command '$@' failed"
        exit 1
    fi
}

# Commands
echo "Compiling the file..."
java advMain "../examples/$filename"
echo "SUCCESS: The compiled .py file has been saved to bin folder"
