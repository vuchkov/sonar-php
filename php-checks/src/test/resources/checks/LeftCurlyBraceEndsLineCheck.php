<?php

function f ()
{                                // NOK
}

if (true)                        // NOK
{
}

$a = function () { doSomething;  // NOK
};

function f() {                   // OK
}

$var->
{'user_' . $id};                 // OK

if (true) { doSomething(); }     // OK

$var->{'user_' . $id};

{                                // OK
  echo $a;
}

if (true) { // TODO false positive ?>
html
<?php }

if (true) { ?> html <? } // NOK
