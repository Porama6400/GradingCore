#include <iostream>
#include <list>

int main() {
    // put each input line in the list
    std::list<std::string> lines;
    std::string line;
    while (std::getline(std::cin, line)) {
        int length = line.length();
        for(int i = length - 1; i >= 0; i-- ){
            if(line[i] != ' '){
                line = line.substr(0, i + 1);
                break;
            }
        }
        lines.push_back(line);
    }

    // remove empty line at the bottom
    if (lines.back().empty()) {
        lines.pop_back();
    }

    // print the list in order
    for (std::list<std::string>::iterator it = lines.begin(); it != lines.end(); ++it) {
        std::cout << *it << std::endl;
    }
}
