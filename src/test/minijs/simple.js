if (true || false) {
    console.log(1);
}

if (false || false || true) {
    console.log(2);
}

if (false && false && true) {
    console.log(3);
}

if (true && false && true) {
    console.log(4);
}

if (true && true && true) {
    console.log(5);
}

var a = 1 || 6;
console.log(a);

a = 1 && 7;
console.log(a);

a = 0 && 8 || 2;
console.log(a);

a = 0 || 9 || 2;
console.log(a);

a = 0 && 10 && 2;
console.log(a);

var test = 126663;
a = 123 + 123 % 55 * (123 / 11 * (5) + 123) / test * 123 - 123 + 100 % 3;
console.log(a);

var x1 = 1, x2 = 2, x3 = 3;
console.log(x3);

if (1 > 1) {
    console.log(11);
}

if (1 >= 1) {
    console.log(12);
}

if (1 < 1) {
    console.log(13);
}

if (1 <= 1) {
    console.log(14);
}

if (2 > 1) {
    console.log(15);
}

if (3 >= 1) {
    console.log(16);
}

if (-1 < 1) {
    console.log(17);
}

if (-5 <= -5) {
    console.log(18);
}

function x() {
    console.log(123);
    return "1";
}

b = c = f = "5" + (x() + "1");
console.log(b, c);
