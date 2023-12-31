int add(int a, int b);
int sub(int a, int b);

int main(char argv, int argc) {

    int a = 10;    
    int b = add(2*add(2*a++, 9), 5*8);

    return 0;
}

int add(int a, int b) {
    return a+b;
}

int sub(int a, int b) {
    return a-b;
}