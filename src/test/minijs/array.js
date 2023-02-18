var a = [1, 2, 3];
a.length = 5;

for (var i = 0; i < a.length; i = i + 1) {
    console.log(a[i]);
}

a.length = 2;

for (var i = 0; i < a.length; i = i + 1) {
    console.log(a[i]);
}

function aa() {
    return 123;
}

var b = [aa() + 1, (aa() - 10000) / 2];
for (var i = 0; i < b.length; i = i + 1) {
    console.log(b[i]);
}

b.c = 1;
console.log(b.c);
delete b.c;
console.log(b.c);

b[-1] = -1;
console.log(b[-1]);
delete b[-1];
console.log(b[-1]);
