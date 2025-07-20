package dev.olaxomi.backend.model;

import java.util.*;

public class Test {
    public static void main(String[] args){
        Car car1 = new Car();
        Car car2 = new Car();
        Car car3 = new Car();
        Car car4 = new Car();

        Set<String> listset = new HashSet<>();

        car1.setColor("Red");
        car1.setMake("Toyota");
        car1.setModel("Avensis");
        car2.setColor("Red");
        car2.setMake("BMW");
        car2.setModel("I7");
        car3.setColor("Blue");
        car3.setMake("Nissan");
        car3.setModel("Bluebird");
        car4.setColor("Red");
        car4.setMake("Hyundai");
        car4.setModel("Elantra");

        List<Car> cars = new ArrayList<>();
        cars.add(car1);
        cars.add(car2);
        cars.add(car3);
        cars.add(car4);
//        cars.stream().filter(c -> c.getColor() == "Blue").forEach(c -> System.out.println(c.toString()));
        cars.stream().map(Car::getModel).forEach(car -> System.out.println(car.toString()));
//        cars.removeAll(Collections.singleton(car1));
//        cars.stream().forEach(car -> System.out.println(car.toString()));
    }
}


