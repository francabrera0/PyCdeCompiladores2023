void printf(int toPrint);

int main() {
    char begin = 'a';
    char end = 'z';

    for(int letter = begin; letter <= end; letter++) {
        printf(letter);
    }
    return 0;
}

void printf(int toPrint) {
    toPrint = toPrint;
}