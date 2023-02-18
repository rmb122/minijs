var a = [1, 2, 3];
a.length = 5;

for (var i = 0; i < a.length; i = i + 1) {
    console.log(a[i]);
}

a.length = 2;

for (var i = 0; i < a.length; i = i + 1) {
    console.log(a[i]);
}
