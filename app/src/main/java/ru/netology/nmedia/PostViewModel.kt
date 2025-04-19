package ru.netology.nmedia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryFileImpl
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl
import ru.netology.nmedia.repository.PostRepositorySharedPrefsImpl

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    published = ""
)

//class PostViewModel: ViewModel() {
class PostViewModel(application: Application): AndroidViewModel(application) {

//    private val repository: PostRepository = PostRepositoryInMemoryImpl()
//    private val repository: PostRepository = PostRepositorySharedPrefsImpl(application)
    private val repository: PostRepository = PostRepositoryFileImpl(application)

    val data = repository.getAll()

    val edited = MutableLiveData(empty)

    fun edit(post: Post = empty) {
        edited.value = post
    }

    fun changeContentAndSave(text: String) {
        edited.value?.let {
            if (it.content != text) {
                repository.save(it.copy(content = text))
            }
        }
        edited.value = empty
    }

    fun likeById(id: Long) = repository.likeById(id)

    fun shareById(id: Long) = repository.shareById(id)

    fun removeById(id: Long) = repository.removeById(id)
}