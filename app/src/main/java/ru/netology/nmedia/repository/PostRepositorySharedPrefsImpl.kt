package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositorySharedPrefsImpl (
    context: Context
): PostRepository {

    private val gson = Gson()
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val key = "posts"
    private var nextId: Long = 1L
    // для этого свойства сделали кастомный сеттер, который всегда вызывает sync()
    var posts = emptyList<Post>()
        set(value) {
            field = value
            data.value = posts
            sync()
        }
    private val data = MutableLiveData(posts)


    init {
        prefs.getString(key, null)?.let{
            posts = gson.fromJson(it, type)
            nextId = (posts.maxOfOrNull { it.id } ?: 0) + 1
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        posts = posts.map{ post: Post ->
            if (post.id != id) post else post.copy(likedByMe = !post.likedByMe, likes = if(!post.likedByMe) post.likes + 1 else post.likes - 1)
        }
    }

    override fun shareById(id: Long) {
        posts = posts.map{post: Post ->
            if(post.id != id) post else post.copy(reposts = post.reposts + 1)
        }
    }

    override fun removeById(id: Long) {
        posts = posts.filter{
            it.id != id
        }
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++, author = "Me")) + posts
        } else {
            posts.map{
                if(it.id != post.id) it else it.copy(content = post.content)
            }
        }
    }

    fun sync() {
        with(prefs.edit()) {
            putString(key, gson.toJson(posts))
            apply()
        }
    }
}