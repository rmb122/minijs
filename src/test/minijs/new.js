function log() {
    console.log(this.c);
}

function test() {
    this.a = 123;
    this.log = log;
    this.c = {log: log, c: "ccc"};
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

a.c.log();
a.log();

c = 555;
log();
