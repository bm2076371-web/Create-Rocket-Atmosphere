# Create: Rocket Atmosphere (v0.2.0)

Mod para Minecraft 1.21.1 / NeoForge (efeitos e detector no cliente; **o Raptor 3 e os fluidos exigem o mod também no servidor**) que adiciona efeitos de lançamento ao
**Create Propulsion: Simulated 1.1.5**: fumaça de ignição espalhada no chão, fumaça nos motores,
rastro na subida, faíscas de chama e vibração de câmera por distância. Não altera nenhum arquivo do
Create Propulsion.

## Novidades da v0.2.0

### 1. Detector de espaço (cliente)

Analisado no jar do Create: Cosmonautics 26.08.307 (modId `rocketnautics`). Como ele realmente funciona:

| Altitude (Y + deslocamento da dimensão) | O que o Cosmonautics faz | Onde está no código |
|---|---|---|
| 1000 | neblina e céu começam a mudar; sol/lua vanilla somem; estrelas aparecem | `SkyHandler.onRenderFog`, `LevelRendererMixin` |
| 2000 | neblina totalmente removida | `clamp((h-1000)/1000)` |
| 3000 | céu totalmente preto | `clamp((h-1000)/2000)` |
| 5000 | Overworld passa a ter `LOW_DENSITY` + `DROWNING` | `universe_planets/overworld.json`, `GlobalSpacePhysicsHandler.getFlags` |
| 20000 | o foguete é transferido para a dimensão de espaço profundo | `SpaceTransitionHandler` (`OVERWORLD_SPACE_Y`, `dimension_transfer_height`) |

* **Detector simples** (`SpaceDetector.isAboveFixedAltitude`): altitude fixa configurável, padrão **3000** (céu totalmente
  preto). Config `space.fixedAltitude` ou `/rocketatmosphere space altitude <valor>`.
* **Detector ambiental** (`SpaceDetector.sample` -> `SpaceState`): dimensão espacial (`DeepSpaceHelper.isDeepSpace`),
  flags de atmosfera do planeta, densidade do ar (`DimensionPhysicsData.getAirPressure` do Sable, a mesma chamada que o
  Propulsion usa) e zona `ATMOSPHERE` / `THIN_ATMOSPHERE` / `SPACE`. Sem Cosmonautics ele continua funcionando (só Sable +
  altitude). A integração com o Cosmonautics é por **reflexão** (sem ligar contra o código GPLv3 dele).
* Os efeitos usam o detector: no vácuo some a fumaça e a chama se abre mais; a densidade reduz a fumaça no ar rarefeito.
* `/rocketatmosphere space` mostra o estado na sua posição; `/rocketatmosphere status` mostra quantos motores estão no espaço.

### 2. Motor Raptor 3

Reaproveita a arquitetura do Create Propulsion em vez de criar física própria. Como o Propulsion funciona (jar 1.1.5):

* **Registro:** `DeferredRegister` do NeoForge (não Registrate). Cada motor = `Block` (`ThrusterBlock`, `FACING` + `multi`) +
  `BlockEntity` (`ThrusterBlockEntity`) + tipo de BE + capability de fluidos.
* **Empuxo -> estrutura inteira:** o BE implementa `BlockEntitySubLevelActor` do Sable. A cada tick de física,
  `sable$physicsTick(ServerSubLevel, RigidBodyHandle, dt)` aplica o impulso na estrutura rígida inteira usando
  `getThrustDirectionLocal()` e `getCurrentThrust()` via `SimulatedThrustAdapter`. Estruturas em movimento são sub-níveis do Sable.
* **Potência 0–100%:** redstone 0–15 (e entrada digital) -> `getPower()`; `getEffectiveThrottle()` aplica a rampa de partida.
* **Combustível:** fluido no `tank` + definição de combustível data-driven (`FluidThrusterProperties`); `oxidizerTank` opcional
  (bônus) só existe em multiblock; `consumeFuelWithAccumulator` consome mB por tick.
* **Atmosfera:** `calculateAtmosphericFactor()` reduz o empuxo com a pressão do ar (o Raptor sobrescreve para 1.0).

O que o Raptor 3 faz (`raptor/Raptor3BlockEntity`, herda de `ThrusterBlockEntity`):

* **Metano (CH4)** no `tank` e **LOX** em um segundo tanque; cada um só aceita o seu fluido (tags `rocketatmosphere:methane` e
  `rocketatmosphere:lox`; `northstar:methane` entra na tag se existir).
* `isWorking()` = metano **e** LOX; faltou um, o Propulsion zera o empuxo no próximo update. O LOX é consumido na razão
  volumétrica 1,33 mB por mB de metano (Raptor: O/F ~3,6 em massa).
* `calculateAtmosphericFactor() = 1.0`: **não depende de ar**, funciona no vácuo.
* Empuxo e consumo 4x o Thruster comum (`THRUST_MULTIPLIER`, ajuste de balanceamento). Bocal em `getNozzleOffsetFromCenter() = 1.5`
  (o modelo tem 2 blocos de comprimento).
* Entrada de fluidos por qualquer lado exceto o do bocal. Goggles mostram metano e LOX.
* Fluidos próprios (metano líquido e LOX) com baldes na aba criativa "Create: Rocket Atmosphere". Receita do motor:
  `data/rocketatmosphere/recipe/raptor_3.json` (usa o Thruster do Propulsion). Não há receitas dos fluidos ainda.
* **Modelo** (`tools/gen_assets.py`): 117 elementos, bocal de 2 blocos de altura, proporções medidas na foto (topo prateado,
  turbobombas, tubo grande, flange, garganta, sino preto, número "1"). `tools/raptor3-preview.png` compara com a foto.
* Efeitos visuais do Raptor: os mesmos do Rocket Atmosphere (qualidade LOW/MEDIUM/HIGH, orçamento de partículas) mais a pluma
  do Propulsion; no vácuo o jato se abre.

### Verificado x não verificado nesta versão

* Verificado no bytecode: todas as assinaturas usadas do Propulsion e do Cosmonautics (listadas nos comentários e nos stubs).
* **Não verificável aqui (o build confirma):** os stubs em `com/simibubi/**` (Create) são minhas reconstruções mínimas a partir do
  javap; `FluidTank.setValidator/getCapacity`, `registerSimpleBlockItem`, `FluidType.Properties`, `IClientFluidTypeExtensions`.
* O `oxidizerTank` do Propulsion só é criado em multiblock, então o Raptor cria o dele; no Create, dois
  `SmartFluidTankBehaviour.single` compartilham o mesmo tipo (o Propulsion faz igual), então o Raptor segue o mesmo padrão.
* O Create: Cosmonautics é GPLv3: por isso a integração é por reflexão e opcional.

## Motores suportados (v0.1.1)

| Motor | Perfil |
|---|---|
| Thruster (1x1, 2x2, 3x3), Solid Fuel, Liquid Vector, Creative (pluma PLUME) | combustao: chamas, fumaca, rastro, nuvem de chao, vibracao |
| Vector (FE), Ion, Creative Vector, Creative (pluma PLASMA/ION) | eletrico: faiscas brilhantes, fumaca leve azulada, vibracao mais fraca (config `electricThrusters`) |
| Creative com pluma NONE | sem efeitos |

Correcao da v0.1.1: na v0.1.0 todo motor com `isIon() == true` era ignorado. No Propulsion o
Vector (FE) e o Creative Vector herdam do motor ionico, entao ficavam sem efeitos. A direcao do
escape dos Vector (inclinada pelo vetor) ja vem de `getWorldExhaustRay()`, entao a nuvem de chao e
o rastro seguem a inclinacao.

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
   e extraia `rocketatmosphere-0.2.0.jar`.
4. Se ficar vermelho: abra o passo *Build*, copie o erro e mande no chat.

No PC: `gradle build` (Gradle 8.8+, Java 21); o jar fica em `build/libs/`.

## Instalar e testar

1. Coloque `rocketatmosphere-0.2.0.jar` na pasta de mods (Battly: junto dos outros .jar). Requer
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
