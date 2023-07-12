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
echo "Cleaning the directory..."
execute_command antlr4-clean
echo "Compiling and building..."
execute_command antlr4 adv.g4
execute_command antlr4-build
echo "The adv compiler has been sucessfully compiled and built!"
