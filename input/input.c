double average(int a, int b, int c);

int main() {
    int a, b, c;

    a = 10*5;
    b = 5;
    c = ++a;

    double result = average(a, b, c);

    
    return result;
}

double average(int a, int b, int c) {
    int acc = 0;
    for(int i=0; i<3; i++) {
        if(i==0)
            acc = acc + a;
        else if(i==1)
            acc = acc + b;
        else
            acc = acc + c;
    }

    return acc/3;
}