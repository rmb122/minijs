function a(asd) {
    asd = asd + "asd";
    return asd;
}

var b = {aa: 1, bb: a("asd")};
console.log(b.aa, b.bb);

delete b.aa;
console.log(b.aa, b.bb);
