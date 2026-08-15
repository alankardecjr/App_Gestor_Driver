# Arquitetura do projeto

## Objetivo

Definir uma estrutura organizada para que o Gestor Driver possa evoluir de forma limpa e sustentável.

## Visão geral

O projeto é composto por:
- uma camada de lógica de negócio;
- uma camada de interface Android;
- uma camada futura de persistência e integração.

## Camadas propostas

### 1. Core / Domain
Responsável por regras de negócio, modelos e cálculos.

### 2. UI
Responsável pela experiência visual e interação com o usuário.

### 3. ViewModel
Controla o estado da interface e comunica com a camada de domínio.

### 4. Repository / Data
Futura camada de persistência e acesso a dados.

### 5. Service
Responsável por integrações com notificações, overlay e outros serviços do sistema.

## Diretrizes

- manter lógica e interface separadas;
- favorecer componentes reutilizáveis;
- preparar o projeto para escalabilidade futura;
- documentar decisões importantes de arquitetura.
