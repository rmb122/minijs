function a(asd) {
    asd = asd + "asd";
    return asd;
}

var b = {aa: 1, bb: a("asd")};
console.log(b.aa, b.bb);

delete b.aa;
console.log(b.aa, b.bb);


function tt() {
    console.log(1, this.b);
    return {a: tt, b: 2};
}

var t = {b: 1, tt: tt};
t.tt().a();
t.tt();
(0, t.tt)();


function f1() {
    console.log(this.c);
}

function f2() {
    console.log(this.c);
    f1();
}

c = 5;
var b = {f2: f2, c: 1};
b.f2();
