function test() {
    this.a = 123;
}

console.log((new test()).a);

var a = new test();
console.log(a.a);
delete a.a;
console.log(a.a);

b = 1;
console.log(b);
delete b;
console.log(b);
