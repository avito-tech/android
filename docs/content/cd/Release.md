---
title: Release
type: docs
---

# Релиз приложения Android

Канал для коммуникации: [#regression-android](http://links.k.avito.ru/slackregressionandroid)

[Dashboard релизов](http://links.k.avito.ru/bw)

[Политика релизов мобильных приложений](http://links.k.avito.ru/cfxtlwWAg)

## Как сделать фикс

Для каждого релиза отводим ветку `release/<Номер версии>`

1. Отводим ветку от релизной
1. Делаем ПР с фиксом в релизную ветку
1. Создаем ветку от текущего develop
1. Делаем ПР с черри-пиком того же коммита в develop

## Кто сейчас релиз менеджер?

На dashboard'е конкретного релиза есть поле `Release manager`                                         
