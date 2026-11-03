# Create: Rocket Atmosphere (v0.1.0)

Mod **somente de cliente** para Minecraft 1.21.1 / NeoForge que adiciona efeitos de lançamento ao
**Create Propulsion: Simulated 1.1.5**: fumaça de ignição espalhada no chão, fumaça nos motores,
rastro na subida, faíscas de chama e vibração de câmera por distância. Não altera nenhum arquivo do
Create Propulsion.

## Status honesto

* Código-fonte completo, com checagem de sintaxe feita (sem erros de parse).
* **NÃO foi compilado** onde foi escrito: o ambiente não tinha rede nem os jars do Minecraft/NeoForge.
  O `.jar` sai do GitHub Actions (abaixo). Se o build acusar erro, mande o log que eu corrijo.
* **NÃO foi testado no jogo.** Os números de efeitos (escala, quantidade, intensidade) são pontos de
  partida e ficam em `QualityPreset.java` / config.

## Como gerar o .jar (só com celular)

1. Crie um repositório no GitHub (pode ser privado) e envie o conteúdo desta pasta
   (Add file → Upload files). A pasta oculta `.github/workflows/build.yml` pode não subir pelo
   seletor do Android: nesse caso crie o arquivo na mão (Add file → Create new file, nome
   `.github/workflows/build.yml`) e cole o conteúdo.
2. Aba **Actions** → workflow **Build** → *Run workflow* (ou ele roda sozinho a cada push).
3. Ao terminar (verde), abra a execução → **Artifacts** → `create-rocket-atmosphere-jar` → baixe o zip
   e extraia `rocketatmosphere-0.1.0.jar`.
4. Se ficar vermelho: abra o passo *Build*, copie o erro e mande no chat.

No PC: `gradle build` (Gradle 8.8+, Java 21); o jar fica em `build/libs/`.

## Instalar e testar

1. Coloque `rocketatmosphere-0.1.0.jar` na pasta de mods (Battly: junto dos outros .jar). Requer
   Create Propulsion 1.1.5+ (e, portanto, Sable/Create que ele já exige).
2. No jogo: `/rocketatmosphere status` → deve mostrar `gancho ativo=ainda nao` até um motor existir
   por perto. Ligue um motor (qualquer propulsor não-iônico): o status passa a `gancho ativo=sim`.
   O `latest.log` mostra `[RocketAtmosphere] gancho ativo...`.
3. Checklist:
   * Ignição no chão: nuvem cinza se espalhando rente ao solo; vibração leve que cresce.
   * Subida: rastro de fumaça atrás do foguete; a nuvem do chão para ao sair do alcance e se dissipa.
   * Afastar-se: a vibração cai com a distância e some devagar (~1,5 s) quando o motor desliga.
   * Troque a qualidade: `/rocketatmosphere quality low|medium|high`.
4. Config: `config/rocketatmosphere-client.toml` (qualidade, distância máxima, escala da fumaça,
   liga/desliga de chama/rastro/chão, força da vibração).

## Arquitetura

```
Create Propulsion (cliente, 1x por tick por motor)
  AbstractThrusterBlockEntity.tick -> ThrusterSoundHooks.clientTick(motor)
                                              |  (Mixin @Inject HEAD, require=0)
                                              v
ThrusterTracker  -- filtros: ativo? nao-ionico? controlador? -> estado leve por motor (WeakHashMap)
   |-- getWorldExhaustRay()  (posicao/direcao do bocal em coordenadas de MUNDO, ja projetada do Sable)
   |-- velocidade do foguete = delta do bocal entre leituras (sem depender da API do Sable)
   |-- CameraShake.addSource(potencia x queda^2 com a distancia)
   '-- ThrusterEffects.emit(...)   (so dentro da distancia maxima, espacado por qualidade)
          |-- chamas: particulas vanilla FLAME/SMALL_FLAME no bocal
          |-- fumaca/rastro: ThrusterSmokeParticleData (particula do proprio Propulsion)
          '-- nuvem de chao: raycast no eixo do escape -> fumaça tangente a superficie
CameraShake: ClientTickEvent.Post (suaviza) + ViewportEvent.ComputeCameraAngles (aplica)
```

Otimização: perfis LOW/MEDIUM/HIGH (distância, densidade, intervalo, vida, alcance de chão);
orçamento global de partículas por tick; LOD por distância; corte por distância máxima; raycast e
leitura do bocal espaçados; config lida 1x/segundo; partículas "forçadas" só além de 28 blocos
(o vanilla descarta partículas normais além de ~32 blocos); sem fumaça em ar rarefeito.

## O que foi verificado no bytecode do jar (javap)

Usado como está no Propulsion 1.1.5: `AbstractThrusterBlockEntity` (`getEffectiveThrottle`,
`isVisuallyActive`, `isMultiblock`, `isController`, `isIon`, `getWorldExhaustRay` + record
`WorldExhaustRay(level, nozzlePos, direction)`, campo público `width`), `ThrusterSoundHooks.clientTick`
(estático, chamado só no cliente), `ThrusterSmokeParticleData(float scale, int lifetime, r, g, b)`,
`DimensionPhysicsData.getAirPressure(Level, Vector3dc)` (estático), `Level.addParticle` (com e sem
`force`), `Level.clip(ClipContext)`, `@EventBusSubscriber(modid, value, bus)`, `ClientTickEvent.Post`,
`RegisterClientCommandsEvent`, `ModConfigSpec`, `ModContainer.registerConfig(Type, spec, nome)`.
As classes em `dev/propulsionteam/**` e `dev/ryanhcode/**` são **stubs só de compilação** com essas
assinaturas; o build exclui esses pacotes do jar.

## Não verificável aqui (o build/jogo confirma)

* `ViewportEvent.ComputeCameraAngles` (get/setPitch/Yaw/Roll): o Propulsion não usa câmera; é API
  pública do NeoForge, mas não está no jar fornecido.
* `ConfigValue.save()`, `CommandSourceStack.sendSuccess`, partículas vanilla `FLAME`/`SMALL_FLAME`.
* Versão do plugin `net.neoforged.moddev` (2.0.78) e `neo_version=21.1.265` em `gradle.properties`:
  se o Gradle não achar, troque por versões existentes.
* Unidade/escala de `getAirPressure`: o valor é limitado a 0..1; se vier em outra unidade, a fumaça
  simplesmente não diminui com a altitude (desligue em `fadeWithAirPressure`).

## Limitações conhecidas

* O Propulsion não expõe evento/registro de motores nem API de "foguete": a única forma de saber de
  todos os motores no cliente é o gancho Mixin acima. Se uma versão futura renomear
  `ThrusterSoundHooks.clientTick`, os efeitos deixam de aparecer (sem crash; o status mostra
  `gancho ativo=ainda nao`).
* Não existe "foguete" como entidade: tratamos cada motor (bloco 2x2/3x3 conta como um). A vibração
  soma os motores próximos com saturação suave e peso maior para motores maiores.
* O raycast de chão pode acertar blocos do próprio foguete se o Sable incluir sub-níveis no `clip`
  (ajuste o recuo `0.6` em `ThrusterEffects.ground`).
* O orçamento de partículas por tick é global e não prioriza o motor mais próximo.
* Motores iônicos não geram fumaça/chama/vibração (não são combustão).
