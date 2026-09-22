// #include "Calculator.h"
//
// int main()
// {
//     Calculator calculator;
//
//     int result = calculator.add(10, 20);
//
//     return result;
// }

#include "Calculator.h"

int calculate()
{
    return 42;
}

int main()
{
    Calculator calculator;

    Calculator* ptr = &calculator;

    int result = calculator.add(10, 20);

    result = calculator.add(30, 40);

    result = result + 5;

    int value = calculate();

    return result + value;
}