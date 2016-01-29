[![License](http://img.shields.io/badge/license-LGPL-blue.svg?style=flat)](https://www.gnu.org/licenses/lgpl-3.0.en.html#content)
[![Boot](https://img.shields.io/badge/boot-2.0.0-ECC42F.svg?style=flat)](http://boot-clj.com/) [![Clojars](https://img.shields.io/badge/clojars-0.1.1-blue.svg?style=flat)](https://clojars.org/zilti/boot-midje)

boot-midje
==========

**Forked from https://bitbucket.org/zilti/boot-midje**

`[zilti/boot-midje "0.1.1"]`

A Boot task allowing you to conveniently use Midje with your Boot projects.

- Note that `midje --autotest` doesn't need `watch`, since it uses Midje's built-in autotest functionality.

### Example usages

- `boot midje`
- `boot midje --autotest wait`

### Available options

<pre>
Options:
-h, --help                  Print this help info.
-t, --test-paths TESTPATH   Conj TESTPATH onto additional paths where the test files reside (analogous to :source-paths).
                            A partial namespace ending in a '*' will load all sub-namespaces.
                            Example: `(load-facts 'midje.ideas.*)`
`
-n, --namespaces NAMESPACE  Conj NAMESPACE onto symbols of the namespaces to run tests in.
-a, --autotest              Use Midje's built-in autotest.
-s, --sources SOURCE        Conj SOURCE onto sources to be watched by autotest; both filenames and directory names are accepted.
-f, --filters FILTER        Conj FILTER onto midje filters. Only facts matching one or more of the arguments are loaded. The filter arguments are:

                            :keyword      -- Does the metadata have a truthy value for the keyword?
                            "string"      -- Does the fact's name contain the given string? 
                            #"regex"      -- Does any part of the fact's name match the regex?
                            a function    -- Does the function return a truthy value when given the fact's metadata?
`
-c, --config CONFIG         Conj CONFIG onto list of midje config files.
-l, --level LEVEL           Set set Midje's verbosity level using one of the following options:

                            :print-normally    (0) -- failures and a summary.
                            :print-no-summary (-1) -- failures only.
                            :print-nothing    (-2) -- nothing is printed.
                                                   -- (but return value can be checked)
                            :print-namespaces  (1) -- print the namespace for each group of facts.
                            :print-facts       (2) -- print fact descriptions in addition to namespaces.

                            to LEVEL.
</pre>
