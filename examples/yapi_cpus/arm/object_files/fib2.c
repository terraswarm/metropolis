#define SIZE 10

int data[SIZE];

int main()
{
        int i, j, temp, n;

        i = 1;
        j = 0;

        for (n = 0; n < SIZE; n++)
        {
                data[n] = i;
                temp = i + j;
                j = i;
                i = temp;
        }

        for (n = 0; n < SIZE; n++)
        {
                data[n] = i;
                temp = i + j;
                j = i;
                i = temp;
        }


        return 0;
}
