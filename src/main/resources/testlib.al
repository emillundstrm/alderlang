
type Test = Test name testFunc

assert msg cond = if cond (Success True) (Failure msg)

runTest (Test name testFunc) = rsMap name testFunc