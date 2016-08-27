BEE++
=====

BEE++ is a small library for inner needs. It compiles some complex 
constraints into a bunch of primitive BEE constraints.

Sketch of BEE++ syntax
----------------------

#### Variable declaration
* Integer (order) variable: `int a: <domain>`
* Integer (dual) variable: `dual_int a: <domain>`
* Boolean variable: `bool a`

where `<domain>` is a union of ranges or a single range: `0..10` or `-10..-5, -4..4, 10..20`
    
#### Constraints
* Supported operations: 
    * for int: `+`, `*`, `/`, `%`, `min`, `max`, `<`, `<=`, `>`, `>=`, `=`, `!=`
    * for bool: `|`, `&`, `^`, `=`, `->`
    * unary minus for negation, bool and int supported
    
#### Example
```
int x: -10..10
int y: -10..10
int z: -10..10
x * x + y * y + z * z < 100
x + y + -z = 5
```