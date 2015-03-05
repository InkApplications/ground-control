Ground Control
==============

Ground control is a library that provides common utilities for synchronizing
local and remote data. It coordinates reactive callbacks from [RxJava] and
provides interfaces that can be used to create sane local SQLLite repositories
that stay in sync with the remote databases.

This library was designed for use on Android projects, and is particularly
well-suited for projects using [Retrofit]

[RxJava]: https://github.com/ReactiveX/RxJava
[Retrofit]: https://square.github.io/retrofit/

Installing
----------

You can add this to your android project by adding the following to your
`build.gradle` file:

~~~
repositories {
    maven {
        url "https://jitpack.io"
    }
}
dependencies {
    compile 'com.github.InkApplications:ground-control:v0.0.1'
}
~~~

