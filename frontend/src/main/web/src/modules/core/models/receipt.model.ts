
export interface Receipt {
  id: string;
  title: string;
  description: string;
  author: string;
  imageUrl?: string;
  hashes?: string[];
  ingredients?: Ingredient[];
  steps?: ReceiptStep[];
}

export interface Ingredient {
  recipeId?: string;
  ingredientId?: string;
  name: string;
  measure: string;
  amount: string;
}

export interface ReceiptStep {
  id?: string;
  recipeId?: string;
  orderNumber: number;
  title: string;
  description: string;
  imageUrl: string;
}