# 词法分析设计

## 功能

1. 划分单词，提取出类别、值信息
2. 处理注释
3. 统计行号

## 类设计

1. `Lexer` 类，词法分析类（单例模式）
2. `LexType` 类，单词类型枚举类

## Lexer 类接口

1. next()
2. getToken()
3. getLexType()

## Lexer 类成员

1. source
2. curPos
3. token
4. lexType
5. reserveWords
6. lineNum
7. number