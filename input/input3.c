
int mult(int, int);
void printf(int result);

int main() {
    int operand1 = 4*5&&2+3-6||5>8;
    int operand2 = --operand1+3>7 || operand1 == 4;

    int result = mult(operand1, operand2);

    printf(result);
    return 0;
}

void printf(int result) {
    result = result;
}

int mult(int operand1, int operand2){
    int minor;
    int major = 0;

    if(operand1<operand2) {
        minor = operand1;
        major = operand2;
    }
    else {
        minor = operand2;
        major = operand1;
    }

    int acc = 0;
    for(int counter = 1; counter<=minor; counter ++) {
        acc = acc + major;
    }

    return acc;
}