function test() {
    for (var i = 0; i < 16; i = i + 1) {
        console.log(i);
    }

    for (var i = 0; i < 16; i = i + 1) {
        if (i == 3) {
            if (true == true) {
                if (false == false) {
                    console.log("===");
                    for (var b = 123; b < 125; b = b + 1) {
                        console.log(b);
                        break;
                    }
                    console.log("===");
                    break;
                }
            }
            continue;
        }
        console.log(i);
    }

    for (var i = 0; i < 16; i = i + 1) {
        if (i == 3) {
            break;
        }
        console.log(i);
    }
}

test();