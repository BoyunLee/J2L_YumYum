import type { InventoryItem, Recipe } from '../stores/fridge'

const GMS_API_URL = import.meta.env.VITE_GMS_API_URL ?? '/gms-api/v1/chat/completions'
const GMS_MODEL = import.meta.env.VITE_GMS_MODEL ?? 'gpt-5-nano'

interface ChatCompletionResponse {
  choices?: Array<{ message?: { content?: string } }>
  error?: { message?: string }
}

interface RecommendedRecipe {
  name: string
  description: string
  matchRate: number
  cookTime: number
  servings: number
  difficulty: string
  calories: number
  availableIngredients: string[]
  missingIngredients: string[]
  steps: string[]
  tips: string[]
}

const gradients = ['yellow', 'red', 'green', 'lime']

function parseRecommendations(content: string): RecommendedRecipe[] {
  const normalized = content.trim().replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/, '')
  const parsed = JSON.parse(normalized) as { recipes?: RecommendedRecipe[] }

  if (!Array.isArray(parsed.recipes) || parsed.recipes.length === 0) {
    throw new Error('추천 레시피가 포함되지 않은 응답을 받았습니다.')
  }

  return parsed.recipes.filter((recipe) =>
    recipe
    && typeof recipe.name === 'string'
    && Array.isArray(recipe.availableIngredients)
    && Array.isArray(recipe.missingIngredients)
    && Array.isArray(recipe.steps),
  )
}

export async function requestRecipeRecommendations(inventory: InventoryItem[]): Promise<Recipe[]> {
  if (inventory.length === 0) throw new Error('추천에 사용할 냉장고 재료가 없습니다.')

  const ingredients = inventory.slice(0, 30).map((item) => ({
    name: item.name,
    quantity: item.quantity,
    unit: item.unit,
    expiryDate: item.expiryDate,
  }))

  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), 30_000)

  try {
    const response = await fetch(GMS_API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: GMS_MODEL,
        messages: [
          {
            role: 'developer',
            content: [
              '당신은 냉장고 재료를 낭비하지 않도록 돕는 한국어 요리 전문가입니다.',
              '주어진 재료와 유통기한을 고려해 만들기 좋은 레시피를 2개 추천하세요.',
              '보유 재료를 우선 사용하고, 없는 재료는 최소화하세요.',
              '설명은 한 문장, 조리 단계는 레시피당 최대 4개, 팁은 1개만 작성하세요.',
              '응답은 마크다운 없이 반드시 {"recipes": [...]} 형태의 유효한 JSON만 반환하세요.',
              '각 레시피는 name, description, matchRate(0~100 정수), cookTime(분 정수), servings(정수), difficulty, calories(정수), availableIngredients(문자열 배열), missingIngredients(문자열 배열), steps(문자열 배열), tips(문자열 배열)를 포함해야 합니다.',
            ].join(' '),
          },
          {
            role: 'user',
            content: `현재 냉장고 재료입니다: ${JSON.stringify(ingredients)}`,
          },
        ],
        response_format: { type: 'json_object' },
        reasoning_effort: 'minimal',
        verbosity: 'low',
        max_completion_tokens: 900,
      }),
      signal: controller.signal,
    })

    const body = await response.json() as ChatCompletionResponse
    if (!response.ok) {
      throw new Error(body.error?.message ?? `레시피 추천 요청에 실패했습니다. (HTTP ${response.status})`)
    }

    const content = body.choices?.[0]?.message?.content
    if (!content) throw new Error('추천 결과가 비어 있습니다.')

    const recommended = parseRecommendations(content)
    if (recommended.length === 0) throw new Error('추천 레시피 형식을 확인할 수 없습니다.')

    const idBase = Date.now()
    return recommended.map((recipe, index) => ({
      id: idBase + index,
      name: recipe.name.trim(),
      description: recipe.description?.trim() || '냉장고 재료로 만드는 추천 요리',
      matchRate: Math.min(100, Math.max(0, Math.round(Number(recipe.matchRate) || 0))),
      cookTime: Math.max(1, Math.round(Number(recipe.cookTime) || 1)),
      servings: Math.max(1, Math.round(Number(recipe.servings) || 1)),
      difficulty: recipe.difficulty?.trim() || '보통',
      calories: Math.max(0, Math.round(Number(recipe.calories) || 0)),
      gradient: gradients[index % gradients.length],
      availableIngredients: recipe.availableIngredients.map(String),
      missingIngredients: recipe.missingIngredients.map(String),
      steps: recipe.steps.map(String),
      tips: Array.isArray(recipe.tips) ? recipe.tips.map(String) : [],
    }))
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error('추천 요청 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.')
    }
    if (error instanceof SyntaxError) throw new Error('추천 결과를 읽지 못했습니다. 다시 시도해 주세요.')
    if (error instanceof TypeError) throw new Error('GMS 서버에 연결하지 못했습니다. 개발 서버와 프록시 설정을 확인해 주세요.')
    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}
