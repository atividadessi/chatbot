package com.example.chatbotia

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Main : ComponentActivity() {
    private lateinit var inputMessage: EditText
    private lateinit var messageContainer: LinearLayout
    private lateinit var sendButton: Button
    private lateinit var messageScrollView: ScrollView
    private lateinit var startButton: Button
    private lateinit var messageUser: String
    private var numberOfPizzas: Int = 0
    private val pizzaChoices = ArrayList<ArrayList<String>>()
    private val pizzaSizes = ArrayList<String>()
    private val pizzaPrices = ArrayList<Double>()
    private var valueEntrega: Double = 6.0
    private var customerName: String = ""
    private var customerAddress: String = ""
    private var erroMensageUser: Int = 0
    private var valueTotalPizzas: Double = 0.0
    private val pizzaOptions = listOf(
        "Calabresa",
        "Marguerita",
        "Pepperoni",
        "4 Queijos",
        "Baianinha",
        "3 Queijos",
        "Frango com Catupiry",
        "Portuguesa",
        "Atum",
        "Bacon",
        "Brocolis",
        "Catupiry",
        "Cheddar",
        "Calabresa com Catupiry",
        "Rúcula com Tomate Seco",
        "Palmito",
        "Carne de Sol",
        "Escarola",
        "Alho e Óleo",
        "Camarão",
        "Mussarela",
        "Califórnia",
        "Quatro Estações",
        "Napolitana",
        "Banana com Canela",
        "Chocolate com Morango",
        "Brigadeiro",
        "Abacaxi com Creme",
        "Morango com Nutella",
        "Romeu e Julieta"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setInitVariables()
        init()
        sendButton()
    }
    private fun setInitVariables() {
        try {
            println("Inicializando variaveis")
            inputMessage = findViewById(R.id.input_message)
            messageContainer = findViewById(R.id.message_container)
            sendButton = findViewById(R.id.send_button)
            messageScrollView = findViewById(R.id.message_scrollview)
            startButton = findViewById(R.id.start_button)
        } catch (exception: Exception){
            println("Erro na inicialização das variaveis" + exception)
        }

    }

    private fun init(){
        sendButton.visibility = View.VISIBLE
        inputMessage.visibility = View.VISIBLE
        val introMessage = "Olá, me chamo NashBot!\n" +
                "Seja bem vindo à nossa pizzaria Nash Pizza's.\nPara agilizar seu atendimento, informe a opção desejada.\n\n1 - Receber cardápio\n2 - Fazer pedido"
        addBotMessage(introMessage)
    }

    private fun getMessage(): String{
        return inputMessage.text.toString()
    }

    private fun sendButton() {
        sendButton.setOnClickListener() {
            messageUser = getMessage()
            if(validMessageUser(2)){
                verificationOptionFromUserInit()
                cleanInputMenssage()
            }
        }
    }

    private fun verificationOptionFromUserInit() {
        when (messageUser.trim()){
            "1" -> {
                addBotMessage( "Aqui estão os sabores disponíveis:\n\n${pizzaOptions.joinToString("\n")}")
                createButtonOrderTemp()
            }
            "2" -> {
                getQuantityPizza()
            }
            else -> {
                if(erroMensageUser < 1){
                    addBotMessage("Por favor digite uma das opções acima")
                    erroMensageUser = erroMensageUser + 1
                }
            }
        }
    }

    private fun createButtonOrderTemp() {
        val button = Button(this)
        button.text = "Fazer o pedido"
        messageContainer.addView(button)
        println("Obtendo quantidade de pizzas")
        button.setOnClickListener {
            getQuantityPizza()
            messageContainer.removeView(button)
        }
    }

    private fun cleanInputMenssage(){
        inputMessage.text.clear()
    }

    private fun getQuantityPizza() {
        addBotMessage("Quantas pizzas você gostaria de pedir ?")

        sendButton.setOnClickListener() {
            messageUser = getMessage()

            if (messageUser.isNotEmpty()) {
                numberOfPizzas = messageUser.toInt()
                addUserMessage()
                println("Quantidade de pizzas: ${numberOfPizzas}")

                cleanInputMenssage()
                selectPizzaSize(0)
            }
        }
    }

    private fun selectPizzaOption(indexPedido: Int) {
        println("Selecionando sabores da pizzas ${indexPedido+1}")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha até três sabores para a pizza ${indexPedido +1}")
        val pizzaChoicesTemp = ArrayList<String>()
        builder.setMultiChoiceItems(pizzaOptions.toTypedArray(), null) { dialog, indexPizza, isChecked ->
            // Adiciona ou remove o sabor selecionado da lista pizzaChoices
            if (isChecked) {
                if (pizzaChoicesTemp.size < 3){
                    pizzaChoicesTemp.add(pizzaOptions[indexPizza])
                } else {
                    // Se o usuário tentar selecionar mais de três sabores, desmarque a opção selecionada
                    (dialog as AlertDialog).listView.setItemChecked(indexPizza, false)
                    showToast("Você só pode selecionar até três sabores.")
                }
            } else{
                pizzaChoicesTemp.remove(pizzaOptions[indexPizza])
            }
        }
        builder.setPositiveButton("OK") { dialog, which ->
            println("indexPedido - ${indexPedido} numero de pedidos - ${numberOfPizzas}")
            println("numero de pizzas pedidas - ${pizzaChoices.size}")
            if (indexPedido < numberOfPizzas) {
                if (pizzaChoicesTemp.size <= 3) {
                    pizzaChoices.add(pizzaChoicesTemp)
                    sendPizzaOrderDetails(indexPedido)
                    println("add pizzaChoiceTemp")
                    if(indexPedido < numberOfPizzas-1){
                        println("Realizando pedido novamente")
                        selectPizzaSize(indexPedido+1)
                    }
                    // Todos os sabores foram selecionados, avance para selecionar o tamanho
                } else {
                    showToast("Selecione até três sabores.")
                }
            }

            if (pizzaChoices.size == numberOfPizzas){
                valueTotalPizzas = calculateTotalAmount()
                addBotMessage("${numberOfPizzas} Pizzas - Valor total: R$ ${valueTotalPizzas}")
                println("Pedidos finalizado - total= ${valueTotalPizzas + valueEntrega}")
                getNameUser()
            }
        }
        builder.show()
    }

    private fun getNameUser() {
        addBotMessage("Digite seu nome")

        sendButton.setOnClickListener() {
            messageUser = getMessage()
            if(messageUser.isNotEmpty()){
                customerName = messageUser
                addUserMessage()
                cleanInputMenssage()
                getAdressClient()
            }
        }
    }

    private fun getAdressClient() {
        addBotMessage("Digite seu Endereço")

        sendButton.setOnClickListener() {
            messageUser = getMessage()
            if(messageUser.isNotEmpty()){
                customerAddress = messageUser
                addUserMessage()
                cleanInputMenssage()
                confirmOrder()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun selectPizzaSize(pedidos:Int) {
        val messageBot = "Pedido: ${pedidos+1}\n\nQual tamanho da pizza: .\n" +
                "\n1 - Pequena" +
                "\n2 - Média" +
                "\n3- Grande" +
                "\n4- Família"
        addBotMessage(messageBot)

        sendButton.setOnClickListener() {
            messageUser = getMessage()
            if(validMessageUser(4)){
                cleanInputMenssage()
                val pizzaSize = getSizePizza()

                pizzaSizes.add(pizzaSize)
                pizzaPrices.add(getPriceForSize(pizzaSize))

                selectPizzaOption(pedidos)
            }
        }
    }

    private fun validMessageUser(indexLimit: Int): Boolean{
        if(messageUser.isEmpty()){
            return false
        }

        if(messageUser.toInt() > 0 && messageUser.toInt() <= indexLimit){
            addUserMessage()
            return true
        }
        return false
    }

    private fun getSizePizza(): String {
        println("Escolhendo tamanho da pizza")
        return when (messageUser.toInt()) {
            1 -> "Pequena"
            2 -> "Média"
            3 -> "Grande"
            4 -> "Família"
            else -> ""
        }
    }

    private fun getPriceForSize(size: String): Double {
        return when (size) {
            "Pequena" -> 20.0
            "Média" -> 25.0
            "Grande" -> 35.0
            "Família" -> 48.0
            else -> 0.0 // Valor padrão, caso o tamanho não seja reconhecido
        }
    }

    private fun sendPizzaOrderDetails(indexPedido: Int) {
        println("Enviando detalhes do pedido ${indexPedido}")

        addBotMessage("Detalhes do pedido ${indexPedido+1}")
        addBotMessage("Tamanho: " + pizzaSizes[indexPedido] + "\n Sabores: " + pizzaChoices[indexPedido] + "\n Valor do pedido: R$ " + pizzaPrices[indexPedido])

    }

    private fun calculateTotalAmount(): Double {
        return pizzaPrices.sum()
    }

    private fun confirmOrder() {
        addBotMessage("Seu pedido será entregue em aproximadamente 5 minutos.")
        addBotMessage("O valor total do seu pedido foi de R$${valueTotalPizzas + valueEntrega}!")
        addBotMessage("Obrigado por escolher Nash Pizza's, $customerAddress!")
        GlobalScope.launch {
            delay(3000)
            notifyOrderInTransit()
        }
    }

    private fun notifyOrderInTransit() {
        addBotMessage("Seu pedido está a caminho!")
        thankForOrder()
    }


    private fun thankForOrder() {
        addBotMessage("Pedido entregue! Aproveite sua refeição!")

    }

    private fun addUserMessage() {
        addMessage("\nEu", messageUser, R.drawable.user_avatar, true)
        scrollToBottom()
    }

    private fun addBotMessage( message: String) {
        addMessage("\nNashBot", message, R.drawable.bot_avatar, false)
        scrollToBottom()
    }

    private fun addMessage(sender: String, message: String, avatarResId: Int, isUserMessage: Boolean) {
        val layoutId = if (isUserMessage) R.layout.message_item_user else R.layout.message_item_bot
        val messageView = layoutInflater.inflate(layoutId, null)

        val messageTextView = messageView.findViewById<TextView>(R.id.message_text)
        val senderTextView = messageView.findViewById<TextView>(R.id.sender_text)
        val avatarImageView = messageView.findViewById<ImageView>(if (isUserMessage) R.id.user_avatar_image else R.id.bot_avatar_image)

        messageTextView.text = message
        senderTextView.text = sender
        avatarImageView.setImageResource(avatarResId)

        val gravity = if (isUserMessage) Gravity.END else Gravity.START
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = gravity
        messageView.layoutParams = params

        messageContainer.addView(messageView)
    }

    private fun scrollToBottom() {
        messageScrollView.post {
            messageScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}