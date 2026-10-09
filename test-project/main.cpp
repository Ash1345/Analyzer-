
#include "Calculator.h"

int calculate()
{
    return 42;
}

int main()
{
    // =====================================================
    // Existing Calculator test cases
    // =====================================================

    Calculator calculator;

    Calculator* ptr = &calculator;

    int result = calculator.add(10, 20);

    result = calculator.add(30, 40);

    double decimalResult =
            calculator.add(10.5, 20.5);

    result = result + 5;

    int value = calculate();


    // =====================================================
    // Polymorphism test case
    // =====================================================



    Car car;
    Vehicle* first = &car;
    first->start();

    Truck truck;
    Vehicle* second = &truck;
    second->start();



    // =====================================================
    // Return result
    // =====================================================

    return result + value;
}
