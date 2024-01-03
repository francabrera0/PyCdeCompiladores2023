int getMajor(int, int);

int main() {
    int a = 3*4+5;
    int b = a++*2/5;

    int major = getMajor(a, b);

    while(major) {
        major = major -1;
    }

    return 0;
}

int getMajor(int operand1, int operand2) {
    
    int major = 0;
    if(operand1 > operand2)
        major = operand1;
    else
        major = operand2;

    return major;
}