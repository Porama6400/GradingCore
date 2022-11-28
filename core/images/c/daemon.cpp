#include <iostream>
#include <string>

int main() {
    std::string command;

    while(1) {
        getline(std::cin, command);

        if(command == "start"){
            system("sudo -u grader gcc main.c -o main && time -v -o timing.txt sudo -u grader ./main > output.txt 2> error.txt");
            std::cout << "Done" << std::endl;

        } else if(command == "ping") {
            std::cout << "Pong" << std::endl;

        } else if(command == "exit") {
            std::cout << "Goodbye!" << std::endl;
            break;

        } else {
            std::cout << "Unknown command" << std::endl;

            for(int i = 0; i < command.length(); i++){
                std::cout << (int) command[i] << ' ';
            }
            std::cout << std::endl;
        }
    };

    return 0;
}