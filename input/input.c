int suma(int, int);

char printf(char);

void helloWorld();

int increment(int);


int main()
{
    int operand1 = 0, operand2 = 0;

    int result = suma(suma(operand1, 5), operand2);
    int s = 0;
    s++;
    if(result > 5)
        printf('5');
    else if(result > 3)
        printf('3');
    else{
        while(1) {
            printf('n');
        }
    }

    for(int iterator = 0; iterator < 10; increment(iterator)) {
        helloWorld();
    }

    return 0;
}

int suma(int operand1, int operand2)
{
    return operand1+operand2;
}

char printf(char toPrint)
{
    return toPrint;
}

void helloWorld()
{
    printf('H');
}

int increment(int toIncrement)
{
    return toIncrement++;
}