---
title: Minimization
type: docs
---

# Минимизация сборки

[Официальная документация](https://developer.android.com/studio/build/shrink-code#shrink-code)

В проекте используется R8 ([задача](http://links.k.avito.ru/MBS6221) на включение "full mode")

Параметры для сборки задаются в корневом `gradle.properties`

В каких типах сборки включена минимизация: [Типы сборки]({{< relref "BuildTypes.md" >}})

[Story "Защита от ошибок минификации кода"](http://links.k.avito.ru/MBS6605) 

## Конфигурация

Общая конфигурация собирается по частям из нескольких источников. 
Помимо способов описанных в [документации](https://developer.android.com/studio/build/shrink-code#configuration-files), конкретно в нашем проекте используется:

- `$ANDROID_HOME/tools/proguard/proguard-android-optimize.txt` базовый конфиг
- `<app>/proguard/**/*.pro` конфигурации разбитые по папкам-типам-сборки, а затем отдельным файлам зависимостям.
Собираются в `build.gradle` при помощи своей функции `proguardFromDir()`

Посмотреть результирующий конфиг сейчас негде, [есть задача](http://links.k.avito.ru/MBS7105) на добавление его в артефакты

## Статьи по теме

- [Как перестать бояться Proguard и начать жить](https://habr.com/ru/post/415499/)
