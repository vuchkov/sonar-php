<?php

posix_kill(42, 42); // Noncompliant {{Make sure that sending signals is safe here.}}
posix_kill(1, 0); // Noncompliant
posix_kill_other_function(1, 0);
