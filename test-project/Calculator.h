#pragma once

class Vehicle
{
public:
    virtual void start();
};

class Car : public Vehicle
{
public:
    void start() override;
};


class Truck : public Vehicle
{
public:
    void start() override;
};


class Calculator
{
public:
    int add(int a, int b);
    double add(double a, double b);
};