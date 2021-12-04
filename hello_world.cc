#include <iostream>

extern const char kBuildScmRevision[];
extern const char kBuildUser[];

int main()
{
    std::cout << "BUILD_USER: " << kBuildUser << std:: endl;
    std::cout << "BUILD_SCM_REVISION: " << kBuildScmRevision << std:: endl;
    return 0;
}
