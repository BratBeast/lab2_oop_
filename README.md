# Лабораторна робота 2 — варіант 7

Холодна зброя. Java 17, Maven, JUnit 5, JaCoCo. Приклади — умовні дані для перевірки XML.

## Запуск в IntelliJ IDEA
1. Розпакувати архів і відкрити папку `lab2-variant7` (або її `pom.xml`).
2. Обрати JDK 17 або новішу, завантажити Maven-проєкт.
3. Запустити `ua.edu.lab2.Main`. Робоча папка — корінь проєкту.
4. Програма виведе об'єкти трьома парсерами і створить `output/grouped.xml`.

## Команди в терміналі
```sh
mvn clean verify
java -jar target/cold-weapons-1.0.0.jar
```
Перший запуск Maven потребує інтернету для завантаження залежностей.
Звіт тестів: `target/surefire-reports`. Покриття: `target/site/jacoco/index.html`.
Власний файл та шлях результату:
```sh
java -jar target/cold-weapons-1.0.0.jar input.xml output/my-groups.xml
```
Програма читає стандартні XML/XSD/XSL із ресурсів JAR, тому працює і поза IDE.

## Структура
- `knives.xml` — корінь `Knife`, повторювані `Item` з обов'язковими унікальними `id`.
- `knives.xsd` — XSD 1.0: прості і комплексні типи, enumeration, minInclusive, xs:ID.
- `Handle` містить рівно один елемент: `Wood` з непорожнім типом дерева, `Plastic` або `Metal`.
- `Knife` і вкладений `Visual` — Java records, створені вручну відповідно до схеми.
- DOM обходить дерево; SAX приймає події; StAX сам читає події в циклі.
- `KnifeBuilder` усуває повторення перетворення полів, підтримує boolean `true/false/1/0`.
- Кожен парсер спочатку виконує XSD-валідацію. Помилки не ігноруються.
- `KnifeComparator` сортує за довжиною, за рівності — за ID.
- XSLT 1.0 групує записи за `Type` у `KnifeGroups/Group/Item`, зберігаючи всі поля.
- Вихід має іншу структуру і не призначений для валідації вхідною `knives.xsd`.
- `logging.properties` налаштовує java.util.logging, INFO/SEVERE та UTF-8.

Поточна реалізація приймає XML у byte[], тому весь вхід зберігається в пам'яті. SAX і StAX не будують DOM-дерево, але це не повністю потокове читання файлу з диска.

## Репозиторій
https://github.com/BratBeast/lab2_oop_

Щоб отримати проєкт разом з історією комітів:
```sh
git clone https://github.com/BratBeast/lab2_oop_.git
cd lab2_oop_
```
Після власних змін:
```sh
git add .
git commit -m "Describe the change"
git push
```

Перевірено: 30 тестів успішні; JaCoCo — 98,4% рядків та 94,2% гілок.
