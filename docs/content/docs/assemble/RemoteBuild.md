---
title: Mirakle
type: docs
---

# Remote build

{{<avito page>}}

Для удаленной сборки используем плагин [Mirakle](https://github.com/Instamotor-Labs/mirakle).\
Он переносит сборку проекта на более мощную машину в дата центре. 
Компьютер во время сборки не так нагружен, можно продолжать работать с проектом.

{{<mermaid>}}
sequenceDiagram
    💻->>+Builder: Держи проект (rsync)
    Note right of Builder: Собираю
    Builder-->>💻: Промежуточные результаты
    Builder->>-💻: Закончил
{{</mermaid>}}


## Как настроить в первый раз?

- Установи rsync
- Посмотри какой host прописан в `mirakle.py`
- Проверь доступность удаленной машины по ssh (с VPN):

```sh
ssh [<username>@]<mirakle host>
```

Если нет доступа:
    - Проверь есть ли уже ключ ([checking for existing SSH keys](https://help.github.com/en/enterprise/2.15/user/articles/checking-for-existing-ssh-keys)) 
    или [сгенерируй новый](https://confluence.atlassian.com/bitbucketserver/creating-ssh-keys-776639788.html).    
    Нужен ключ без пароля, потому что из mirakle (Gradle) его некуда вводить.
    - Добавь свой публичный ssh ключ - [инструкция (internal)](http://links.k.avito.ru/QP)
        - Создай PR в указанный репозиторий
        - Попроси в чатике [#devops](https://avito.slack.com/archives/C02D4DCQ2) влить его
    - Запроси в [Service Desk (internal)](http://links.k.avito.ru/uZ) доступ по ssh на host android-builder
- Включи mirakle: 

```sh
./mirakle.py --enable
```

`username` будет взят из `git user.email`

- Проверь работу, запусти любую задачу: `./gradlew help`   
В логе будут сообщения:

```text
Here's Mirakle ...

:uploadToRemote

:executeOnRemote

:downloadInParallel
```

В следующие разы нужно только включать `./mirakle.py --enable`

## Как отключить?

```sh
./mirakle.py --disable
```

Чтобы отключить только для текущей сборки, добавь в аргументы Gradle `-x mirakle`   

Полное удаление настроек: 

```sh
/mirakle.py --wipe
```

## Troubleshooting

- Проверь что подключен VPN
- Проверь доступность mirakle по ssh, подключись вручную:

```sh
ssh [<username>@]<mirakle host>
``` 

- Проверь [known issues]({{<relref "#known-issues">}})

## Known issues

### Запустилось не то, что я запускал

Проверь что синхронизация проекта проходит успешно.

### Сборка в mirakle идет дольше чем локальная

- Копирование проекта на другую машину занимает время. Если собираешь что-то небольшое, то сравни с локальной сборкой. Она может оказаться быстрее. 
- На удаленной машине накопились старые файлы в проекте, rsync их не удалил.   
Удали их: 

```sh
./clean.py --remote
```
