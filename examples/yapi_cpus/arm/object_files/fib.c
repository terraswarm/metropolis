#define SIZE 10

int data[SIZE];

int main()
{
        int i, j, temp, n, r;

        r = 1234;
        i = 1;
        j = 0;

        for (n = 0; n < SIZE; n++)
        {
                data[n] = i;
                temp = i + j;
                j = i;
                i = temp;
        }
        r = 12345;


        return 0;
}
